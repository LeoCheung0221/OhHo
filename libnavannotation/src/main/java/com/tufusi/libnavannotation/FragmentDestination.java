package com.tufusi.libnavannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by 鼠夏目 on 2020/9/21.
 *
 * @author 鼠夏目
 * @description
 */

@Target(ElementType.TYPE)
public @interface FragmentDestination {

    String pageUrl();

    boolean needLogin() default false;

    boolean asStarter() default false;
}
