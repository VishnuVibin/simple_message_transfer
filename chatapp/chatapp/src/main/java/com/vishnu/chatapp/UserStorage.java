package com.vishnu.chatapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserStorage {

    public static Set<String> users =
            ConcurrentHashMap.newKeySet();

    public static String admin = null;

    public static synchronized void addUser(String username) {

        users.add(username);

        if (admin == null) {

            admin = username;
        }
    }

    public static synchronized boolean removeUser(String username) {

        boolean removed = users.remove(username);

        if (username.equals(admin)) {

            admin = users.isEmpty()
                    ? null
                    : users.iterator().next();
        }

        return removed;
    }

    public static synchronized void setAdmin(String username) {

        if (users.contains(username)) {

            admin = username;
        }
    }

    public static boolean isAdmin(String username) {

        return admin != null
                && admin.equals(username);
    }

    public static synchronized String toJson() {

        List<String> userList = new ArrayList<>(users);
        StringBuilder jsonUsers = new StringBuilder("[");

        for (int i = 0; i < userList.size(); i++) {

            if (i > 0) {

                jsonUsers.append(",");
            }

            jsonUsers.append("\"")
                    .append(userList.get(i).replace("\\", "\\\\").replace("\"", "\\\""))
                    .append("\"");
        }

        jsonUsers.append("]");

        return "{\"users\":" + jsonUsers + ",\"admin\":\"" + (admin == null ? "" : admin) + "\"}";
    }
}