# Ando-Compiler
[![CodeFactor](https://www.codefactor.io/repository/github/ando233/ando-compiler/badge)](https://www.codefactor.io/repository/github/ando233/ando-compiler)
![](https://img.shields.io/github/languages/code-size/Ando233/Ando-Compiler)
![](https://img.shields.io/github/contributors/Ando233/Ando-Compiler)

## 使用方法

在Compiler下与src同级的testfile.txt中编写要编译的sysY代码，运行Compiler，随后你将在output.txt中看到输出。

Tips：如果要看到语法分析结果，将Compiler.java中ASTDump下面的几行全部注释掉就可以了(

## lab总览

> lab流程参考文档：https://buaa-se-compiling.github.io/miniSysY-tutorial与https://pku-minic.github.io/online-doc

### lab-1: 文法解读

> 主要是根据文法文件写测试程序，达到覆盖率100%

这阶段看着简单也挺折磨的，好多次写完程序之后提交发现不符合文法要求，然后还要再一句一句对文法 :(

### lab0: 词法分析

### lab1: main与注释

这一部分难度主要在于设计一个合理的架构，实现最最基础的前端，中端。

架构和语言的选择很重要，我从java转到c++又转回java。。。一定要选自己熟练的语言~~或者周围大佬熟练的语言~~(

架构也是如此，我在lab1重构了一下代码，新增了buildFactor工厂模式，不然后面visitor会很麻烦。

### lab2: 表达式

#### lab2-1: 一元表达式



#### lab2-2: 算数表达式



#### lab2-3: 比较和逻辑表达式



## lab3: 常量与变量

#### lab3-1: 常量

#### lab3-2: 变量与赋值

## lab4: 语句块与作用域

## lab5: if语句

#### lab5-1: 处理if/else

#### lab5-2: 短路求值

## lab6: while语句

#### lab6-1: 处理while

#### lab6-2: break和continue

## lab7: 函数和全局变量

#### lab7-1: 函数定义和调用

#### lab7-2: SysY库函数

#### lab7-3: 全局变量和常量

## lab8: 数组

#### lab8-1: 一维数组

#### lab8-2: 多维数组

#### lab8-3: 数组参数

## lab9: 优化

#### lab9-1: 寄存器分配

#### lab9-2: 更多优化
