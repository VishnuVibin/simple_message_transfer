let stompClient = null;

let username = "";

let role = "USER";

let selectedRecipient = "";

let selectedAdminTarget = "";

let allUsers = [];

let currentAdmin = null;

const displayedMessageIds = new Set();

const messageHistory = [];

const unreadCounts = {};

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

function validateLogin(usernameValue, passwordValue) {

    const trimmedUsername = usernameValue.trim();
    const trimmedPassword = passwordValue.trim();

    if (trimmedUsername === "" || trimmedPassword === "") {

        return false;
    }

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
function joinChat(selectedRole) {

    const enteredName =
        document.getElementById(
            "usernameInput"
        ).value;

    const enteredPassword =
        document.getElementById(
            "passwordInput"
        ).value;

    if (!validateLogin(enteredName, enteredPassword)) {

        alert("Enter both username and password");

        return;
    }

    if (selectedRole === "ADMIN") {

        if (enteredName !== "Vishnu" || enteredPassword !== "admin123") {

            alert("Admin login requires username:");

            return;
        }
    }

    role = selectedRole;

    if (!setUsername(enteredName)) {

        alert("Enter Username");

        return;
    }

    showChatScreen();
    connectSocket();
}

// Connect WebSocket
function renderUsers(payload) {

    const data =
        typeof payload === "string"
            ? JSON.parse(payload)
            : payload;

    if (data && data.action === "DELETE" && data.targetUser === username) {

        if (stompClient) {

            stompClient.disconnect();
            stompClient = null;
        }

        sessionStorage.removeItem("chatUsername");
        selectedRecipient = "";
        selectedAdminTarget = "";
        showLoginScreen();
        alert("You were removed by the admin. Please log in again.");

        return;
    }

    allUsers =
        Array.isArray(data.users)
            ? data.users
            : String(data.users || "")
                .split(",")
                .map((name) => name.trim())
                .filter(Boolean);

    currentAdmin = data.admin || null;

    updateAdminPanel();
    filterUsers();
}

function selectUser(user) {

    selectedRecipient = user;
    selectedAdminTarget = user;

    document.getElementById(
        "recipientInput"
    ).value = user;

    document.getElementById(
        "chatUser"
    ).textContent =
        "Chat with " + user;

    const publicBtn = document.getElementById("publicChatBtn");
    if (publicBtn) {
        publicBtn.style.background = "linear-gradient(135deg, #1e1b4b, #311042)";
        publicBtn.style.borderColor = "#8b5cf6";
    }

    unreadCounts[user] = 0;

    redrawMessages();
    filterUsers();
}

function updateAdminPanel() {

    const adminPanel =
        document.getElementById(
            "adminPanel"
        );

    adminPanel.style.display =
        role === "ADMIN" || currentAdmin === username
            ? "block"
            : "none";
}

function filterUsers() {

    const query =
        document.getElementById(
            "searchUserInput"
        ).value.trim().toLowerCase();

    const users =
        allUsers.filter((user) =>
            user.toLowerCase().includes(query)
        );

    const suggestionBox =
        document.getElementById(
            "suggestionBox"
        );

    const usersList =
        document.getElementById(
            "usersList"
        );

    suggestionBox.innerHTML = "";

    if (query === "") {

        suggestionBox.style.display = "none";
    } else {

        suggestionBox.style.display = "block";

        users.forEach((user) => {

        const item =
            document.createElement(
                "button"
            );

        item.type = "button";
        item.className = "suggestion-item";
        item.textContent = user;
        item.onclick = function () {

            document.getElementById(
                "searchUserInput"
            ).value = user;

            suggestionBox.style.display = "none";
            selectUser(user);
        };

            suggestionBox.appendChild(item);
        });
    }

    usersList.innerHTML = "";

    users.forEach((user) => {

        if (user === username) {

            return;
        }

        const button =
            document.createElement(
                "button"
            );

        const unreadCount = unreadCounts[user] || 0;
        if (unreadCount > 0) {
            button.innerHTML = `${user} <span class="unread-badge" style="background:#ef4444; color:white; border-radius:50%; padding:2px 6px; font-size:0.75rem; margin-left:8px; font-weight:bold; vertical-align:middle;">${unreadCount}</span>`;
        } else {
            button.textContent = user;
        }

        button.className = "user-chip";
        button.onclick = function () {

            selectUser(user);
        };

        if (user === selectedRecipient) {
            button.style.background = "linear-gradient(135deg, #3b82f6, #1d4ed8)";
            button.style.borderColor = "#60a5fa";
        }

        usersList.appendChild(button);
    });
}

function deleteSelectedUser() {

    if (!selectedAdminTarget) {

        alert("Select a user first");

        return;
    }

    stompClient.send(
        "/app/admin/delete",
        {},
        JSON.stringify({
            requester: username,
            targetUser: selectedAdminTarget
        })
    );
}

function promoteSelectedUser() {

    if (!selectedAdminTarget) {

        alert("Select a user first");

        return;
    }

    stompClient.send(
        "/app/admin/promote",
        {},
        JSON.stringify({
            requester: username,
            targetUser: selectedAdminTarget
        })
    );
}

function selectPublicChat() {
    selectedRecipient = "";
    selectedAdminTarget = "";

    document.getElementById(
        "recipientInput"
    ).value = "";

    document.getElementById(
        "chatUser"
    ).textContent =
        "🌐 Public Chat Room";

    const publicBtn = document.getElementById("publicChatBtn");
    if (publicBtn) {
        publicBtn.style.background = "linear-gradient(135deg, #8b5cf6, #22d3ee)";
        publicBtn.style.borderColor = "#a78bfa";
    }

    redrawMessages();
    filterUsers();
}

function redrawMessages() {
    const messagesContainer = document.getElementById("messages");
    messagesContainer.innerHTML = "";
    displayedMessageIds.clear();

    messageHistory.forEach(msg => {
        appendMessageToUI(msg);
    });
}

function appendMessageToUI(msg) {
    const currentIsPublic = (selectedRecipient === "");

    if (currentIsPublic) {
        if (msg.type !== "PUBLIC") {
            return;
        }
    } else {
        if (msg.type !== "PRIVATE") {
            return;
        }
        const isFromRecipient = String(msg.sender).trim() === String(selectedRecipient).trim();
        const isToRecipient = String(msg.recipient).trim() === String(selectedRecipient).trim();
        const isFromMe = String(msg.sender).trim() === String(username).trim();
        const isToMe = String(msg.recipient).trim() === String(username).trim();

        const isExchangedWithRecipient = (isFromMe && isToRecipient) || (isFromRecipient && isToMe);

        if (!isExchangedWithRecipient) {
            return;
        }
    }

    if (msg.clientId) {
        if (displayedMessageIds.has(msg.clientId)) {
            return;
        }
        displayedMessageIds.add(msg.clientId);
    }

    const messages = document.getElementById("messages");
    const div = document.createElement("div");

    const isSentByMe = String(msg.sender).trim() === String(username).trim();

    div.className = isSentByMe ? "message sent" : "message received";

    const label = msg.type === "PRIVATE" ? "Private" : "Public";

    div.innerHTML = `<b>${msg.sender}</b> <small>(${label})</small><br>${msg.content}`;

    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
}

function connectSocket() {

    const socket =
        new SockJS('/chat');

    stompClient =
        Stomp.over(socket);

    stompClient.connect({ username: username }, function () {

        console.log(
            "Connected Successfully"
        );

        stompClient.subscribe(
            "/topic/public",
            function (message) {

                const msg =
                    JSON.parse(
                        message.body
                    );

                displayMessage(msg);
            }
        );

        stompClient.subscribe(
            "/user/queue/private",
            function (message) {

                const msg =
                    JSON.parse(
                        message.body
                    );

                displayMessage(msg);
            }
        );

        stompClient.subscribe(
            "/queue/" + username,
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

        selectPublicChat();
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

    const recipientInput =
        document.getElementById(
            "recipientInput"
        );

    const recipient =
        (recipientInput.value || selectedRecipient || "")
            .trim();

    const clientMessageId =
        "local-" +
        Date.now().toString() +
        "-" +
        Math.random().toString(36).slice(2);

    let message;

    if (recipient === "") {
        message = {
            sender: username,
            content: content,
            type: "PUBLIC",
            clientId: clientMessageId
        };
    } else {
        message = {
            sender: username,
            content: content,
            type: "PRIVATE",
            recipient: recipient,
            clientId: clientMessageId
        };
    }

    stompClient.send(
        "/app/send",
        {},
        JSON.stringify(message)
    );

    messageBox.value = "";
}

// Display Message
function displayMessage(msg) {

    if (msg.type === "PRIVATE") {

        const isForThisUser =
            String(msg.sender).trim() === String(username).trim() ||
            String(msg.recipient).trim() === String(username).trim();

        if (!isForThisUser) {

            return;
        }
    }

    messageHistory.push(msg);

    if (msg.type === "PRIVATE" && msg.sender !== username && msg.sender !== selectedRecipient) {
        unreadCounts[msg.sender] = (unreadCounts[msg.sender] || 0) + 1;
        filterUsers();
    }

    appendMessageToUI(msg);
}

// Press Enter to Send
window.addEventListener(
    "DOMContentLoaded",
    requestUsername
);

document.getElementById(
    "deleteUserBtn"
).addEventListener("click", deleteSelectedUser);

document.getElementById(
    "deleteHeaderBtn"
).addEventListener("click", deleteSelectedUser);

document.getElementById(
    "makeAdminBtn"
).addEventListener("click", promoteSelectedUser);

document.addEventListener(
    "input",
    function (event) {

        if (event.target.id === "searchUserInput") {

            filterUsers();
        }
    }
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