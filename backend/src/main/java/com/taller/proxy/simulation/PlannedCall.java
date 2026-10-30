package com.taller.proxy.simulation;

import java.util.Map;

public record PlannedCall(String operation, Map<String, Object> params) {}
