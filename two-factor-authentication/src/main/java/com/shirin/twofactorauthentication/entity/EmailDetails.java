package com.shirin.twofactorauthentication.entity;

public record EmailDetails(
     String recipient,
     String body,
     String subject
){
}
