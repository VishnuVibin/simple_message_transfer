let stompClient = null;

let username = "";

let role = "USER";

let selectedRecipient = "";

let selectedAdminTarget = "";

let allUsers = [];

let currentAdmin = null;

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

        button.textContent = user;
        button.className = "user-chip";
        button.onclick = function () {

            selectUser(user);
        };

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

    if (recipient === "") {

        alert("Select a user to chat privately with");

        return;
    }

    selectedRecipient = recipient;

    document.getElementById(
        "chatUser"
    ).textContent =
        "Chat with " + recipient;

    const clientMessageId =
        "local-" +
        Date.now().toString() +
        "-" +
        Math.random().toString(36).slice(2);

    const message = {

        sender: username,

        content: content,

        type: "PRIVATE",

        recipient: recipient,

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

    if (msg.type === "PRIVATE") {

        const isForThisUser =
            String(msg.sender).trim() === String(username).trim() ||
            String(msg.recipient).trim() === String(username).trim();

        if (!isForThisUser) {

            return;
        }
    }

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