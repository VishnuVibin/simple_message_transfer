let stompClient = null;

let username = "";

let selectedRecipient = "";

const displayedMessageIds = new Set();

function showChatScreen() {

    document.getElementById(
        "loginScreen"
    ).style.display = "none";

    document.getElementById(
        "chatApp"
    ).style.display = "flex";

    document.getElementById(
        "myName"
    ).innerText = username;
}

function showLoginScreen() {

    document.getElementById(
        "loginScreen"
    ).style.display = "flex";

    document.getElementById(
        "chatApp"
    ).style.display = "none";
}

function setUsername(value) {

    username = value.trim();

    if (username === "") {

        return false;
    }

    sessionStorage.setItem(
        "chatUsername",
        username
    );

    return true;
}

function requestUsername() {

    const savedUsername =
        sessionStorage.getItem(
            "chatUsername"
        );

    if (savedUsername) {

        username = savedUsername;
        showChatScreen();
        connectSocket();

        return;
    }

    showLoginScreen();

    const usernameInput =
        document.getElementById(
            "usernameInput"
        );

    usernameInput.focus();
}

// Join Chat
function joinChat() {

    const enteredName =
        document.getElementById(
            "usernameInput"
        ).value;

    if (!setUsername(enteredName)) {

        alert("Enter Username");

        return;
    }

    showChatScreen();
    connectSocket();
}

// Connect WebSocket
function renderUsers(userList) {

    const users =
        userList
            .split(",")
            .map((name) => name.trim())
            .filter(Boolean);

    const usersList =
        document.getElementById(
            "usersList"
        );

    usersList.innerHTML = "";

    users.forEach((user) => {

        if (user === username) {

            return;
        }

        const button =
            document.createElement(
                "button"
            );

        button.textContent = user;
        button.className = "user-chip";
        button.onclick = function () {

            selectedRecipient = user;

            document.getElementById(
                "chatUser"
            ).textContent =
                "Chat with " + user;
        };

        usersList.appendChild(button);
    });
}

function connectSocket() {

    const socket =
        new SockJS('/chat');

    stompClient =
        Stomp.over(socket);

    stompClient.connect({}, function () {

        console.log(
            "Connected Successfully"
        );

        stompClient.subscribe(
            "/topic/messages",
            function (message) {

                const msg =
                    JSON.parse(
                        message.body
                    );

                displayMessage(msg);
            }
        );

        stompClient.subscribe(
            "/topic/private-" + username,
            function (message) {

                const msg =
                    JSON.parse(
                        message.body
                    );

                displayMessage(msg);
            }
        );

        stompClient.subscribe(
            "/topic/users",
            function (message) {

                renderUsers(
                    message.body
                );
            }
        );

        stompClient.send(
            "/app/join",
            {},
            JSON.stringify({
                sender: username
            })
        );
    });
}

// Send Message
function sendMessage() {

    const messageBox =
        document.getElementById(
            "message"
        );

    const content =
        messageBox.value.trim();

    if (content === "")
        return;

    const recipient =
        selectedRecipient.trim();

    const clientMessageId =
        "local-" +
        Date.now().toString() +
        "-" +
        Math.random().toString(36).slice(2);

    const message = {

        sender: username,

        content: content,

        type: recipient ? "PRIVATE" : "PUBLIC",

        recipient: recipient || null,

        clientId: clientMessageId
    };

    stompClient.send(
        "/app/send",
        {},
        JSON.stringify(message)
    );

    messageBox.value = "";
}

// Display Message
function displayMessage(msg) {

    if (msg.clientId) {

        if (displayedMessageIds.has(msg.clientId)) {

            return;
        }

        displayedMessageIds.add(msg.clientId);
    }

    const messages =
        document.getElementById(
            "messages"
        );

    const div =
        document.createElement(
            "div"
        );

    const isSentByMe =
        String(msg.sender).trim() ===
        String(username).trim();

    div.className =
        isSentByMe
            ? "message sent"
            : "message received";

    const label =
        msg.type === "PRIVATE"
            ? "Private"
            : "Public";

    div.innerHTML =
        `<b>${msg.sender}</b> <small>(${label})</small><br>${msg.content}`;

    messages.appendChild(div);

    messages.scrollTop =
        messages.scrollHeight;
}

// Press Enter to Send
window.addEventListener(
    "DOMContentLoaded",
    requestUsername
);

document.addEventListener(
    "keypress",
    function (event) {

        if (
            event.key === "Enter" &&
            document.getElementById(
                "chatApp"
            ).style.display === "flex"
        ) {

            sendMessage();
        }
    }
);