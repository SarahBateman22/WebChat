let ws = new WebSocket("ws://localhost:8080");
let inChatRoom = false;

ws.onopen = console.log('WebSocket connection opened!');

ws.onmessage = function(e) {
    handleIncomingMessage(e);
};

ws.onclose= console.log("Websocket closed.");

//buttons - enter room, leave room, send message
let enterRmBtn = document.getElementById("enterRm");
let leaveRmBtn = document.getElementById("leaveRm");
let sendMsgBtn = document.getElementById("sendMsg");

//break


//text fields
let nameFld = document.getElementById("name");
let roomFld = document.getElementById("room");
let msgFld = document.getElementById("message");

//chat and user space
let usersPg = document.getElementById("users");
let chatsPg = document.getElementById("chats");

enterRmBtn.onclick = handleEnterRmClick;
leaveRmBtn.onclick = handleLeave;
sendMsgBtn.onclick = handleMessageSend;



function handleIncomingMessage(e){
    //making object to work with
    let msgObj = JSON.parse(e.data);
    console.log(msgObj);
    let room = msgObj.room;

    //check to see if the chat room has any upper case or space characters in it
    let containsInvalidChars = false;
    for (let i = 0; i < room.length; i++) {
        const charCode = room.charCodeAt(i);
        if (charCode < 97 || charCode > 122) {
            containsInvalidChars = true;
            break;
        }
    }

    //handle join
    if(msgObj.type === "join"){
        if (containsInvalidChars) {
            alert("Your chat room is invalid. Chat rooms must be all lowercase may not contain spaces.");
        }
        else{
            let name = document.createTextNode(msgObj.user);
            let joinText = document.createTextNode(msgObj.user + " joined " + msgObj.room + ".");
            usersPg.appendChild(document.createElement("br"));
            usersPg.appendChild(name);
            chatsPg.appendChild(document.createElement("br"));
            chatsPg.appendChild(joinText);
        }
    }

    //handle message
    if(msgObj.type === "message"){

        let messageTxt = document.createTextNode(msgObj.user + ": " + msgObj.message);
        chatsPg.appendChild(document.createElement("br"));
        chatsPg.appendChild(messageTxt);
    }

    //handle leave
    if(msgObj.type === "leave"){
        let leaveText = document.createTextNode(msgObj.user + " left " + msgObj.room + ".");
        chatsPg.appendChild(document.createElement("br"));
        chatsPg.appendChild(leaveText);
    }
}

function handleEnterRmClick(){
    if(getName()!=="" && getRoom()!=="" && !inChatRoom){
        ws.send("join:" + getName() + ":" + getRoom());
        inChatRoom = true;
    }

    else if (inChatRoom) {
        alert("You are already in a chat room. Please leave the current room before joining another one.");
    }

    else{
        alert("Incorrect entry, please try again.");
    }

    nameFld.value = "";
    roomFld.value = "";
}

function handleLeave(){
    inChatRoom=false;

    ws.send("leave:" + getName() + ":" + getRoom());

    let you = document.createTextNode("You left the room.");
    chatsPg.appendChild(document.createElement("br"));
    chatsPg.appendChild(you);
    let message = {"type":"leave", "user":nameFld.value, "room":roomFld.value}
    ws.send(JSON.stringify(message));
}

function handleMessageSend() {
    let message = msgFld.value;

    console.log("message is: " + message);

    if (message !== "") {
        ws.send("message:" + getName() + ":" + getRoom() + ":" + message);
        msgFld.value = "";
    } else {
        alert("Entry is null, please try again");
    }
}

function getName(){
    return nameFld.value.toLowerCase();
}

function getRoom(){
    return roomFld.value.toLowerCase();
}

// function getCurrentTimestamp() {
//     const now = new Date();
//     const hours = now.getHours().toString().padStart(2, '0');
//     const minutes = now.getMinutes().toString().padStart(2, '0');
//     return `${hours}:${minutes}`;
// }


//OLD CODE
// let message = {"type":"join", "user":nameFld.value, "room":roomFld.value}
// ws.send(JSON.stringify(message));
// console.log("join " + nameFld.value + " " + roomFld.value);
// console.log("handleEnterRmClick here");