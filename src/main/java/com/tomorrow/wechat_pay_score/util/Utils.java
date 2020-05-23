package com.tomorrow.wechat_pay_score.util;

import java.util.*;

/**
 * @author Tomorrow
 * @date 2020/5/23 1:00
 */
public class Utils {

    /**
     * 判断数组是否存在重复数据，并将重复数据返回
     * @param objects
     * @return
     */
    public static List<Object> judgingAnArray(Object[] objects) {
        Set<Object> set = new HashSet<Object>();
        List<Object> stringList = new ArrayList<>();
        for (int i = 0; i < objects.length; i++) {
            boolean b = set.add(objects[i]);
            if (!b) {
                stringList.add(objects[i]);
            }
        }
        return stringList;
    }

    /**
     *  生成随机数
     * @param length
     * @return
     */
    public static String getRandomString(int length) {
//      创建一个随机数生成器。
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < length; ++i) {
//          返回下一个伪随机数，它是此随机数生成器的序列中均匀分布的 int 值。
            int number = random.nextInt(3);
            long result = 0;
//          Math 类包含用于执行基本数学运算的方法，如初等指数、对数、平方根和三角函数。
//          Math.random()  返回带正号的 double 值，该值大于等于 0.0 且小于 1.0。
//          Math.round(Math.random() * 25 + 97)  返回最接近参数的 long。
            switch (number) {
                case 0:
                    result = Math.round(Math.random() * 25 + 65);
                    sb.append(String.valueOf((char) result));
                    break;
                case 1:
                    result = Math.round(Math.random() * 25 + 97);
                    sb.append(String.valueOf((char) result));
                    break;
                case 2:
                    sb.append(String.valueOf(new Random().nextInt(10)));
                    break;
            }
        }

        return sb.toString();
    }
}
