<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
遍历数据模型中的list学生信息
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>金额</td>
        <td>出生日期</td>
    </tr>
    <#if stus??>
        <#list stus as stu>
            <tr>
                <td>${stu_index+1}</td>
                <td>${stu.name}</td>
                <td>${stu.age}</td>
                <td<#if (stu.money > 300)> style="background:red;"</#if>> ${stu.money}</td>
<#--                <td>${stu.birthday?date}</td>-->
            </tr>
        </#list>
        学生的个数:${stus?size}
    </#if>

</table>
<br/>
遍历数据模型中stuMap的数据  第一种方法在中括号中填写map的key 第二种方法 在map后面直接加点key
<br/>
姓名：${stuMap['stu1'].name} <br/>
年龄：${stuMap['stu1'].age}<br/>

姓名:${(stuMap.stu2.name)!""} <br/>
年龄:${stuMap.stu2.age} <br/>
遍历map中的key <br/>
 <#list stuMap?keys as k>
     姓名:${stuMap[k].name} <br/>
     年龄:${stuMap[k].age} <br/>
 </#list>

${point?c}<br/>

</body>
</html>