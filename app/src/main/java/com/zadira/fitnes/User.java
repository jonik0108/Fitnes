package com.zadira.fitnes;

import java.util.List;

public class User {
    String name;
    List<String> tasks;
    List<Integer> rasm;
    List<Integer> count;
    List<Integer> time;

    public User(String name, List<String> tasks, List<Integer> rasm, List<Integer> count, List<Integer> time) {
        this.name = name;
        this.tasks = tasks;
        this.rasm = rasm;
        this.count = count;
        this.time = time;
    }
}