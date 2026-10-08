'use strict';
const $ = id => document.getElementById(id);
const pageType = document.body.dataset.page;
const money = value => new Intl.NumberFormat('en-IN', {style: 'currency', currency: 'INR', minimumFractionDigits: 2}).format(value);
let state = null;
let category = 'All';
let search = '';
let sort = 'featured';
let busy = false;
let pendingProduct = null;
let toastTimer;
let detailProduct = null;

function node(tag, className = '', text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}
function art(id, className = '') {
  const element = node('div', className);
  // Only trusted, locally authored SVG markup is inserted here, never customer input.
  element.innerHTML = window.NovaArtwork[id] || window.NovaArtwork.hub;
  return element;
}
function button(text, className, callback, label) {
  const element = node('button', className, text);
  element.type = 'button';
  if (label) element.setAttribute('aria-label', label);
  element.addEventListener('click', callback);
  return element;
}
function toast(message, error = false, cartLink = false) {
  clearTimeout(toastTimer);
  $('toast').replaceChildren(node('span', '', message));
  if (cartLink) { const link = node('a', '', 'View cart ↗'); link.href = 'cart.html'; $('toast').append(link); }
  $('toast').className = 'toast show' + (error ? ' error' : '');
  toastTimer = setTimeout(() => $('toast').classList.remove('show'), 4300);
}
function dialogOpen(id) { if (!$(id).open) $(id).showModal(); }
function requestName(productId = null) {
  pendingProduct = productId;
  $('customer-name').value = state?.customerName || '';
  $('name-error').textContent = '';
  dialogOpen('customer-dialog');
  $('customer-name').focus();
}
async function load() {
  try {
    const response = await fetch('api/cart', {cache: 'no-store'});
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Could not retrieve your cart.');
    state = data;
    render();
  } catch (error) {
    toast(error.message, true);
    const target = pageType === 'shop' ? $('products') : $('cart-items');
    target.replaceChildren();
    const box = node('div', 'connection-error', 'Unable to connect to your shopping session.');
    box.append(button('Retry', 'secondary', load));
    target.append(box);
    if ($('results-count')) $('results-count').textContent = 'Connection unavailable';
    if ($('products')) $('products').setAttribute('aria-busy', 'false');
  }
}
async function mutate(action, values = {}, message = 'Your cart has been updated.') {
  if (busy || !state) return false;
  busy = true;
  render();
  try {
    const response = await fetch('api/cart', {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded', 'X-CSRF-Token': state.csrfToken},
      body: new URLSearchParams({action, ...values})
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Could not update your cart.');
    if (data.ended) {
      await load();
      toast('A new shopping session is ready.');
    } else {
      state = data;
      toast(message, false, action === 'add' && pageType === 'shop');
    }
    return true;
  } catch (error) {
    if (action === 'customer') $('name-error').textContent = error.message;
    toast(error.message, true);
    return false;
  } finally {
    busy = false;
    render();
  }
}
async function addProduct(id) {
  if (busy || !state) return;
  if (!state.customerName) { requestName(id); return; }
  const product = state.catalog.find(item => item.id === id);
  if (product) await mutate('add', {id}, product.name + ' added to your cart.');
}
function tagFor(product) {
  if (product.id === 'laptop') return ['THE WORKSPACE HERO', true];
  if (product.id === 'headphones') return ['TUNE INTO YOUR DAY', true];
  if (product.id === 'keyboard-mini') return ['SMALL DESK. BIG IDEAS.', false];
  if (product.id === 'monitor-wide') return ['ROOM TO CREATE', false];
  return ['EVERYDAY ESSENTIAL', false];
}
function productCard(product) {
  const card = node('article', 'product-card');
  const visual = node('div', 'product-visual');
  const image = button('', 'product-art-button', () => showProduct(product.id), 'View details for ' + product.name);
  image.append(art(product.id));
  const [label, highlight] = tagFor(product);
  visual.append(image, node('span', 'product-tag' + (highlight ? ' highlight' : ''), label));
  visual.append(button('Quick view ↗', 'product-quick', () => showProduct(product.id), 'Quick view ' + product.name));
  const copy = node('div', 'product-copy');
  const top = node('div', 'product-topline');
  top.append(node('span', 'product-category', product.category));
  const quantity = state.items.find(item => item.id === product.id)?.quantity || 0;
  if (quantity) { const indicator = node('span', 'product-state'); indicator.append(node('i', 'live-dot'), node('span', '', quantity + ' in cart')); top.append(indicator); }
  copy.append(top, node('h3', '', product.name), node('p', 'product-description', product.description));
  const bottom = node('div', 'product-bottom');
  const add = button('', 'add-button', () => addProduct(product.id), 'Add ' + product.name + ' to cart');
  add.append(node('span', 'plus', '+'), node('span', '', quantity >= 10 ? 'Limit reached' : 'Add to cart'));
  add.disabled = busy || quantity >= 10;
  bottom.append(node('span', 'price', money(product.price)), add);
  copy.append(bottom);
  card.append(visual, copy);
  return card;
}
function renderCatalog() {
  let products = state.catalog.filter(product => (category === 'All' || product.category === category) && (product.name + ' ' + product.category + ' ' + product.description).toLowerCase().includes(search));
  if (sort === 'price-low') products.sort((a, b) => a.price - b.price);
  if (sort === 'price-high') products.sort((a, b) => b.price - a.price);
  if (sort === 'name') products.sort((a, b) => a.name.localeCompare(b.name));
  $('products').replaceChildren(...products.map(productCard));
  $('products').setAttribute('aria-busy', 'false');
  $('results-count').textContent = 'Showing ' + products.length + ' of ' + state.catalog.length + ' essentials';
  $('no-results').hidden = products.length !== 0;
  $('clear-search').hidden = !search;
}
function renderCart() {
  const units = state.itemCount;
  $('page-count').textContent = units + (units === 1 ? ' item' : ' items');
  $('cart-greeting').textContent = state.customerName ? 'A considered selection, put together by ' + state.customerName + '.' : 'Your essentials, all in one place.';
  $('summary-count').textContent = '(' + units + ')';
  $('subtotal').textContent = money(state.total);
  $('total').textContent = money(state.total);
  $('clear-cart').disabled = busy || !units;
  $('review').disabled = busy || !units;
  $('end-session').disabled = busy;
  $('session-customer').textContent = state.customerName || 'Not entered yet';
  $('conversation').textContent = state.conversationId;
  $('revision').textContent = state.revision;
  const container = $('cart-items');
  container.replaceChildren();
  if (!state.items.length) {
    const empty = node('div', 'empty-cart');
    const link = node('a', 'primary', 'Find your essentials ↗'); link.href = 'index.html#collection';
    empty.append(node('span', '', '◇'), node('h2', '', 'A little room for possibility.'), node('p', '', 'Your cart is empty. Let’s find something that belongs on your desk.'), link);
    container.append(empty);
  }
  for (const item of state.items) {
    const row = node('article', 'cart-item');
    row.dataset.product = item.id;
    const product = node('div', 'cart-item-product');
    const copy = node('div', 'cart-item-copy');
    const remove = button('Remove item', 'remove-item', () => mutate('remove', {id: item.id}, item.name + ' removed.'), 'Remove ' + item.name);
    remove.disabled = busy;
    copy.append(node('span', 'product-category', item.category), node('h3', '', item.name), node('p', '', money(item.price) + ' each'), remove);
    product.append(art(item.id, 'cart-item-art'), copy);
    const quantity = node('div', 'quantity-control');
    quantity.setAttribute('aria-label', item.name + ' quantity');
    const decrease = button('−', '', () => mutate('quantity', {id: item.id, quantity: item.quantity - 1}), 'Decrease ' + item.name + ' quantity');
    const increase = button('+', '', () => mutate('quantity', {id: item.id, quantity: item.quantity + 1}), 'Increase ' + item.name + ' quantity');
    decrease.disabled = busy || item.quantity <= 1;
    increase.disabled = busy || item.quantity >= 10;
    const count = node('span', '', item.quantity); count.setAttribute('aria-label', 'Quantity ' + item.quantity);
    quantity.append(decrease, count, increase);
    row.append(product, quantity, node('strong', 'line-subtotal', money(item.subtotal)));
    container.append(row);
  }
  // Recommend actual catalog products not already selected, with deterministic ordering.
  const selected = new Set(state.items.map(item => item.id));
  const suggested = [...state.catalog.filter(product => ['mouse', 'keyboard', 'headphones', 'hub'].includes(product.id)), ...state.catalog];
  const ids = new Set();
  const recommendations = suggested.filter(product => !selected.has(product.id) && !ids.has(product.id) && ids.add(product.id)).slice(0, 4);
  $('recommendations').replaceChildren(...recommendations.map(productCard));
  document.querySelector('.recommended').hidden = recommendations.length === 0;
}
function render() {
  if (!state) return;
  $('header-count').textContent = state.itemCount;
  document.querySelector('.header-cart').setAttribute('aria-label', 'View cart, ' + state.itemCount + ' items');
  $('customer-label').textContent = state.customerName || 'Your session';
  $('profile-button').title = state.customerName ? 'Shopping as ' + state.customerName + ' — edit name' : 'Enter your customer name';
  $('customer-form').querySelector('button').disabled = busy;
  if (pageType === 'shop') renderCatalog(); else renderCart();
  if (detailProduct && $('product-dialog').open) refreshDetailButton();
}
function refreshDetailButton() {
  const add = $('detail-add');
  if (!add) return;
  const quantity = state.items.find(item => item.id === detailProduct)?.quantity || 0;
  add.disabled = busy || quantity >= 10;
  add.querySelector('span:first-child').textContent = quantity >= 10 ? 'Maximum quantity reached' : 'Add to your cart';
}
function showProduct(id) {
  if (!state) return;
  const product = state.catalog.find(item => item.id === id);
  if (!product) return;
  detailProduct = id;
  const content = $('product-detail');
  content.replaceChildren();
  content.append(node('span', 'dialog-kicker', product.category.toUpperCase()), art(id, 'detail-art'), node('h2', 'detail-title', product.name));
  const specs = node('ul', 'detail-specs');
  product.description.split(' · ').forEach(text => specs.append(node('li', '', text)));
  content.append(specs, node('div', 'detail-price', money(product.price)));
  const add = button('', 'primary detail-add', () => { $('product-dialog').close(); addProduct(id); });
  add.id = 'detail-add'; add.append(node('span', '', 'Add to your cart'), node('span', '', '↗'));
  content.append(add, node('p', 'dialog-note', 'Sample INR pricing. Selected products stay in your customer session.'));
  dialogOpen('product-dialog'); refreshDetailButton();
}
function reviewCart() {
  if (!state?.itemCount) return;
  $('review-customer').textContent = 'Prepared for ' + state.customerName + ' · ' + state.itemCount + ' items';
  $('review-lines').replaceChildren();
  for (const item of state.items) { const row = node('p'); row.append(node('span', '', item.name + ' × ' + item.quantity), node('b', '', money(item.subtotal))); $('review-lines').append(row); }
  $('review-total').textContent = money(state.total);
  dialogOpen('review-dialog');
}
$('profile-button').addEventListener('click', () => { if (state) requestName(); });
$('customer-form').addEventListener('submit', async event => {
  event.preventDefault();
  const productId = pendingProduct;
  if (await mutate('customer', {name: $('customer-name').value}, 'Your personal shopping session is ready.')) {
    $('customer-dialog').close(); pendingProduct = null;
    if (productId) await addProduct(productId);
  }
});
$('customer-dialog').addEventListener('close', () => { pendingProduct = null; });
document.querySelectorAll('[data-close]').forEach(element => element.addEventListener('click', () => $(element.dataset.close).close()));
if (pageType === 'shop') {
  $('hero-laptop').append(art('laptop')); $('hero-headphones').append(art('headphones')); $('hero-mouse').append(art('mouse')); $('editorial-keyboard').append(art('keyboard-mini'));
  document.querySelectorAll('.category-tab').forEach(tab => tab.addEventListener('click', () => {
    category = tab.dataset.category;
    document.querySelectorAll('.category-tab').forEach(item => { const active = item === tab; item.classList.toggle('active', active); item.setAttribute('aria-pressed', active); });
    if (state) renderCatalog();
  }));
  $('search').addEventListener('input', () => { search = $('search').value.trim().toLowerCase(); if (state) renderCatalog(); });
  $('clear-search').addEventListener('click', () => { $('search').value = ''; search = ''; renderCatalog(); $('search').focus(); });
  $('sort').addEventListener('change', () => { sort = $('sort').value; if (state) renderCatalog(); });
  $('reset-filters').addEventListener('click', () => {
    search = ''; $('search').value = ''; document.querySelector('[data-category="All"]').click();
  });
} else {
  $('clear-cart').addEventListener('click', () => { if (!busy && state?.itemCount && confirm('Remove every product from your cart?')) mutate('clear', {}, 'Your cart is cleared. Your customer session stays active.'); });
  $('end-session').addEventListener('click', () => { if (!busy && confirm('End this customer session and clear its cart?')) mutate('end'); });
  $('session-toggle').addEventListener('click', () => { const expanded = $('session-toggle').getAttribute('aria-expanded') !== 'true'; $('session-toggle').setAttribute('aria-expanded', expanded); $('session-detail').hidden = !expanded; $('session-toggle').lastElementChild.textContent = expanded ? '−' : '+'; });
  $('review').addEventListener('click', reviewCart);
}
// A back/forward navigation or return to this tab reloads the server snapshot.
window.addEventListener('pageshow', event => { if (event.persisted) load(); });
document.addEventListener('visibilitychange', () => { if (!document.hidden && !busy && state) load(); });
load();
