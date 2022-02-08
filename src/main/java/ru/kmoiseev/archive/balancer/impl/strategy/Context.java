package ru.kmoiseev.archive.balancer.impl.strategy;

import lombok.Value;

import java.util.List;

@Value
public class Context {
    List<String> urls;
}
