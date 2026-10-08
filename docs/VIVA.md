# Quick viva — Hinglish

**Project kya hai?** NovaCart ek online shopping-cart application hai. Customer apna naam enter karke 12 products mein se selection add, view, remove aur clear kar sakta hai. Required Laptop, Mouse aur Keyboard categories included hain. Header ka Cart link separate page kholta hai jahan quantity +/- controls aur totals hain.

**EJB kya hai?** Enterprise JavaBean server-side business component hai. Application server uska lifecycle aur invocation manage karta hai. Hum Jakarta EE ka EJB use kar rahe hain.

**Stateful kyun?** Har customer ke products multiple requests ke beech yaad rakhne hain. Isliye `@Stateful` use kiya hai. Mere cart aur doosre customer ke cart ke liye alag bean instance hota hai.

**State kis variable mein hai?** `customerName` customer ko represent karta hai, `quantities` product ID aur quantity rakhta hai. `conversationId` same bean conversation identify karta hai; `revision` successful updates count karta hai.

**add() kya karta hai?** Product aur customer validate karta hai, existing quantity padhta hai, phir quantity increment karke map update karta hai.

**viewCart() old data kaise laata hai?** Same EJB instance ke `quantities` map ko read karke immutable snapshot return karta hai. Har request par naya cart nahi banta.

**Refresh par data kyu rehta hai?** Browser session cookie bhejta hai. HttpSession mein same EJB proxy retained hai, isliye servlet same Stateful Bean invoke karta hai.

**Stateless difference?** Stateless bean ke liye client-specific conversational state ki guarantee nahi hoti; instances pooled ho sakte hain. Stateful bean particular client conversation ka state maintain karta hai.

**clear aur end mein difference?** Clear sirf products remove karta hai; customer aur bean same rehte hain. End session listener ke through `@Remove` call karta hai, jo container ko bean destroy karne ko bolta hai.

**@ApplicationException kyun?** Expected validation error ko system failure treat karne se bachata hai. Isse invalid input ke baad Stateful Bean discard nahi hota.

**Tomcat pe chalega?** Plain Tomcat EJB container nahi deta. Hum Payara Micro 6 use karte hain.

**Database kyun nahi?** Assignment conversational cart state demonstrate karne ka hai. Persistent orders ya real payments scope mein nahi hain. Server restart/timeout ke baad persistence promise nahi karte.

**Demo proof?** Items add karo, conversation ID note karo, refresh karo. Same items aur ID retained rahenge. Incognito mein alag empty cart aur alag ID dikhega.

**Dono pages pe same state kaise?** index.html aur cart.html same session cookie ke saath /api/cart ko call karte hain. Servlet same session-specific EJB proxy retrieve karta hai. Page change hone se cart bean replace nahi hota.

**Header count kya count karta hai?** Total units. Agar ek Mouse quantity 2 hai aur ek Laptop hai, header count 3 hoga.
