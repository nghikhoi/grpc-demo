package me.nghikhoi.grpcclientdemo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

@AllArgsConstructor
public class AbstractSpringApplication {

    @Getter private final ApplicationContext context;

}
