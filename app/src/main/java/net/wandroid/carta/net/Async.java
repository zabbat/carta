package net.wandroid.carta.net;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Method might be called asynchronous and some operations
 * such as FragmentTransaction are  not allowed
 */
@Target(ElementType.METHOD)
public @interface Async {
}
