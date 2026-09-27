# 3W影视
# 当前版本不启用代码混淆，这里保留扩展规则文件。
# 后续如果启用 R8，再根据 Media3、Gson 等依赖补充规则。

# 保留 Gson 使用的模型字段
-keepattributes Signature
-keepattributes *Annotation*

-keep class com.threew.tv.model.** { *; }

# 保留 Gson 序列化/反序列化需要的字段
-keepclassmembers class com.threew.tv.model.** {
    <fields>;
}

# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
