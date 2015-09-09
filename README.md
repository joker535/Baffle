Android apk包res 资源混淆工具

功能描述：
对编译后的APK 进行资源名称混淆。混淆之后apk对比如下图：
 ![image](https://raw.githubusercontent.com/joker535/image/master/baffle1.jpg)
 
目前支持命令行操作：
java -jar baffle.jar [-c/--config filepaths list ][-o/--output filepath] ApkFile TargetApkFile

-c/--config 可选项，支持keep和mapping配置文件。
-o/--output 可选项，支持混淆后mapping文件输出。

config文件格式见res目录，描述如下：
<pre><code>
\#这个开头的行是注释。
\#key keep 段，比较关键。需要用名字取得资源需要keep，支持正则表达式。 key指的是R文件里的变量名。不是文件名，不包含后缀
----keep_key

notificationsound
newicon
\#下面这个是，java的正则表达式。
^mini.*

----keep_key

\#key 段的mapping
----map_key

activity_login,bx
imexaple,a
icon,b
activity_myinformation,ae
----map_key
\#大概就是这个样子，暂时不支持include之类的操作。和keep规则冲突以mapping为准
</pre></code>
