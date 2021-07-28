# DigitalPantry
 A stock managment system designed primarily for home use.

### The Project

This project is comprised of an android app and a web server. 

### Use

The app is configured to use my localhost and the ip of my computer and so the first thing is to change the IP in the app project files, which can be found at "...\DigitalPantry\AndroidApp\DigitalPantryApp\app\src\main\java\com\hurst\digitalpantry\Config.java". Swap the WEB_SERVER_BASE value out for the ip of the machine you will be running the web server on. Note the default port is 7000, if you want to change this that will require editing the web server file too.

Next, we need to actually start the web server, which is done by running node server.js from a terminal in the web server directory (...\DigitalPantry\WebServer).

With the server started the last thing to do is run the app on a device. This can be done by launching android studio and using it to install the app on a plugged in device. Note, the app can be run on an emulator but you won't be able to actually scan any barcodes which is the point of the app. For the app to scan barcodes it also requires that you install a different app called 'Barcode Scanner' by ZXing Team, which can be found on the google play store.

### The App

The app currently has 3 screens, 1 to add products by scanning their barcodes, 1 to see the current items in the pantry and 1 to see a distinct list of all the products that have been scanned before (recognised products).

### The pantry

The default screen will show all the products and how many there are in the pantry currently.

![image](https://user-images.githubusercontent.com/43950567/127306741-1920d7c1-cfd8-46ca-908c-7974335df0f0.png)


### Adding products

The screen named 'Session' is where products can be scanned and added. As the name suggests products added here are not actually in the pantry yet, this is a sort of staging area where you can add multiple products and then confirm them (with the blue tick action button). To add a product press the pink '+' button and the barcode app will launch allowing you to scan an item. The program then checks if it has seen that product before (in which case it's added to the session), and if it hasn't then it will prompt you to give details about the product. On the 'Session' screen you can swipe products left and right to decrease or increase the quantity of that item respectively. 

![image](https://user-images.githubusercontent.com/43950567/127305310-9543235a-13cb-4cd8-843d-ec476ec3d28b.png)


### Product Database

The last screen will show you all the distinct products you have registered/entered and allow you to edit their details.

![image](https://user-images.githubusercontent.com/43950567/127305962-4f0d0a08-03cc-4f21-83ab-03afd687ef09.png)





