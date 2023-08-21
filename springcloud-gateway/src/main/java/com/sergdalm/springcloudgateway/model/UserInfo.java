package com.sergdalm.springcloudgateway.model;

import lombok.Value;

import java.util.UUID;

@Value
public class UserInfo {

    UUID id;
    String name;
}
