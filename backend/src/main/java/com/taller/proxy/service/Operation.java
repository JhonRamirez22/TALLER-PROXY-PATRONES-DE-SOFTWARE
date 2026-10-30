package com.taller.proxy.service;

import java.util.Map;

/** Patron Command: cada operacion de un microservicio es un objeto invocable y registrable. */
@FunctionalInterface
public interface Operation {
    Object run(Map<String, Object> args);
}
