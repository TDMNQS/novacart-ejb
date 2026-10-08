"""Run against an actual Payara EJB deployment: python tests/integration.py."""
import json, urllib.request, urllib.parse, urllib.error, http.cookiejar, sys
BASE = sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080/novacart/api/cart'
class Client:
 def __init__(self):
  self.jar=http.cookiejar.CookieJar();self.http=urllib.request.build_opener(urllib.request.ProxyHandler({}),urllib.request.HTTPCookieProcessor(self.jar));self.state=self.get()
 def get(self):
  with self.http.open(BASE) as r:self.state=json.load(r)
  return self.state
 def post(self,action,token=True,**values):
  headers={'Content-Type':'application/x-www-form-urlencoded'}
  if token:headers['X-CSRF-Token']=self.state['csrfToken']
  req=urllib.request.Request(BASE,data=urllib.parse.urlencode(dict(action=action,**values)).encode(),headers=headers)
  try:
   with self.http.open(req) as r:result=json.load(r);status=r.status
  except urllib.error.HTTPError as e:return e.code,json.load(e)
  if 'items' in result:self.state=result
  return status,result
count=0
def check(ok,label):
 global count
 assert ok,label
 count+=1;print('PASS',label)
a=Client();b=Client();id_a=a.state['conversationId'];id_b=b.state['conversationId']
check(id_a!=id_b,'distinct container-managed conversations')
check(len(a.state['catalog'])==12,'expanded server catalog contains 12 products')
check(a.post('add',id='laptop')[0]==400,'name required')
check(a.post('customer',name='Numan')[0]==200,'customer accepted')
check(a.post('add',token=False,id='laptop')[0]==403,'CSRF rejected')
for product in ['laptop','mouse','keyboard']:check(a.post('add',id=product)[0]==200,'add '+product)
check(a.state['total']==69788,'exact server-owned total')
revision=a.state['revision'];a.get();check(a.state['revision']==revision and a.state['itemCount']==3 and a.state['conversationId']==id_a,'refresh retains EJB state')
b.post('customer',name='Furqan');check(b.state['itemCount']==0,'independent customer has empty cart')
a.post('add',id='mouse');check(a.state['itemCount']==4 and a.state['total']==71087,'repeat add increments quantity')
check(a.post('quantity',id='mouse',quantity=0)[0]==400,'reject zero quantity')
check(a.post('quantity',id='mouse',quantity=11)[0]==400,'reject excessive quantity')
check(a.post('add',id='invalid')[0]==400,'reject unlisted product')
check(a.post('quantity',id='mouse',quantity='two')[0]==400,'reject noninteger quantity')
a.post('remove',id='keyboard');check(a.state['total']==67588,'remove product recalculates total')
a.post('clear');check(a.state['itemCount']==0 and a.state['customerName']=='Numan' and a.state['conversationId']==id_a,'clear preserves customer conversation')
a.post('end');a.get();check(a.state['conversationId']!=id_a and not a.state['customerName'],'end destroys previous conversation')
a.post('customer',name='Numan')
a.post('add',id='headphones');a.post('add',id='hub')
check(a.state['total']==8298,'new category products use server-owned prices')
a.post('quantity',id='headphones',quantity=2)
check(a.state['total']==14297 and a.state['itemCount']==3,'increase new product quantity')
a.post('quantity',id='headphones',quantity=1)
check(a.state['total']==8298 and a.state['itemCount']==2,'decrease new product quantity')
before=a.state['revision'];a.post('quantity',id='headphones',quantity=0)
check(a.get()['revision']==before and a.state['total']==8298,'invalid quantity keeps same EJB usable')
print(f'{count} integration checks passed.')
