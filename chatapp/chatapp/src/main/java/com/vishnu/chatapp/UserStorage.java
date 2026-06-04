package com.vishnu.chatapp;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserStorage {

    public static Set<String> users =
            ConcurrentHashMap.newKeySet();
}