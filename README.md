# CodeModelDemo
A demo of JCodeModel

##CodeModel代码生成

###常用类
在 CodeModel中,常用的类有JCodeModel、JDefinedClass、JMethod、JBlock、JFieldVar、JVar、JType、JExpr 等。
####JCodeModel
JCodeModel 类是生成 Java 代码的根。通过它生成JDefinedClass类，然后再生成构造方法、成员变量、方法以及方法体等。从上而下，依次生成。
<code>

	JCodeModel codeMode = new JCodeModel();
    JDefinedClass genClass = codeMode._class("com.example.test.TestMain",
				ClassType.CLASS);
    ...
    codeModel.build(new File("src"));
</code>
上述代码片段就会在src目录下，生成一个TestMain类
####1.JDefinedClass 
JDefinedClass类是通过CodeModel来定义类的，它提供了类自身的创建、继承、实现，以及类成员变量、成员方法的创建方法等
<code>

    genClass._extends(Student.class);
    genClass._implements(Serializable.class);
</code>
如上代码是继承Student类，实现Serializable接口
####2.JMethod 
JMethod 类是Java的方法类，它可以创建方法体，那么就有了JBlock类
<code>

	JMethod jMethod = genClass.method(JMod.PUBLIC + JMod.STATIC,
				void.class, "fun");// public static void fun()
    jMethod.param(codeModel.parseType("String"), "str1");
	jMethod.param(codeModel.parseType("String"), "str2");
</code>
上述代码，首先生成了一个fun方法，然后分别生成了两个参数，生成的代码是：
<code>

	public static voud fun(String str1,String str2){
    }
</code>
####3.JBlock 
JBlock类是经常要用到的类，它提供了非常多的方法：局部变量的声明、变量赋值、生成各种控制语句、调用其他方法、设置方法的返回值等。上面代码，我们已经可以生成方法了，但是方法中的内容怎么生成呢？
<code>

	JBlock block = jMethod.body();//获取方法的方法体
	JVar nameVar = block.decl(codeModel.parseType("Student"), "name");//声明一个局部变量
</code>
生成的代码如下：
<code>

	public static void fun(String str1, String str2) {
        Student name;
    }
</code>
####4.JFieldVar 
JFieldVar类用来定义类的成员变量，它可以对成员变量进行声明、赋值等
<code>

	JFieldVar sFieldVar = genClass.field(JMod.PRIVATE + JMod.STATIC,
				codeModel.INT, "sCount", JExpr.lit(10));
</code>
生成的代码：
<code>

	private static int sCount = 10;
</code>
####5.JVar 
JVar类用来定义局部变量，提供了变量的基本操作如声明、赋值、初始化等
<code>

	JBlock block = jMethod.body();
	JVar nameVar = block.decl(codeModel.parseType("Student"), "name");
	block.assign(nameVar,JExpr._new(codeModel.parseType("Student")));
</code>
生成赋值的代码：
<code>

	public static void fun(String str1, String str2) {
        Student name;
        name = new Student();
    }
</code>
####6.JType 
JType 类用来定义Java中的各种数据类型
常用的类型，比如int,void等基本类型，可以通过codeModel获取，比如codeModel.INT,codeModel.VOID等。如果是自定义类型，比如Student类。
<code>

	JType type = codeModel.parseType("Student");
</code>
####7.JExpr 
JExpr 类表达式的工厂类，它提供了 TRUE 和 FALSE 两个常量值，以及生成表达式的各种方法以及表达式的赋值等
####8.JMod
主要是方法或者成员变量的属性，比如private、public、static、synchronized等，使用‘+’连接符。

###举例
源码地址：https://github.com/nuptboyzhb/CodeModelDemo.git

例如，如下代码是生成一个Utils类

<code>

	public class GenUtilsDemo {

	public static void main(String[] args) {
		try {
			genClass();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void genClass() throws Exception {
		JCodeModel codeMode = new JCodeModel();
		File destDir = new File("src");// 代码生成的目录
		JDefinedClass genClass = codeMode._class("net.mobctrl.aa.Utils",
				ClassType.CLASS);// 生成Utils类
		genClass._implements(Serializable.class);
		// 生成静态成员变量
		JFieldVar sUtilsField = genStaticField(codeMode, genClass);
		// 生成私有构造方法
		genPrivateConstructor(codeMode, genClass);
		// 生成单例
		genGetInstanceMethod(codeMode, genClass, sUtilsField);
		// 生成fun方法
		genFunMethod(codeMode, genClass);
		// 生成filter方法
		genFilterMethod(codeMode, genClass);

		codeMode.build(destDir);
	}

	/**
	 * 生成代码
	 * 
	 * <pre>
	 * private void filter(int[] datas, int bottom) {
	 * 	for (int i = 0; i &lt; datas.length; i++) {
	 * 		if (datas[i] &gt; bottom) {
	 * 			System.out.println(datas[i]);
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param codeMode
	 * @param genClass
	 */
	private static void genFilterMethod(JCodeModel codeMode,
			JDefinedClass genClass) throws Exception {
		JMethod filterMethod = genClass.method(JMod.PRIVATE,
				codeMode.parseType("void"), "filter");// 生成方法
		filterMethod.javadoc().add("doc: filter datas");//添加注释
		filterMethod.param(codeMode.parseType("int []"), "datas");// 添加参数int []
																	// datas
		filterMethod.param(codeMode.parseType("int"), "bottom");// 添加参数int
																// bottom
		

		JBlock methodBody = filterMethod.body();
		JForLoop forLoop = methodBody._for();// 生成for循环
		forLoop.init(codeMode.parseType("int"), "i", JExpr.lit(0));// 循环的初始 int
																	// i = 0;
		forLoop.test(JExpr.direct("i<datas.length"));// 循环条件：直接写表达式
		forLoop.update(JExpr.ref("i").incr());// 每次循环

		JBlock forBody = forLoop.body();// 获取for循环的内部体
		JConditional ifConJConditional = forBody._if(JExpr.ref("datas[i]").lt(
				JExpr.ref("bottom")));// 在for循环中生成if(datas[i]<bottom)条件语句
		JBlock ifBody = ifConJConditional._then();// 获得if的模块体
		JBlock elseBody = ifConJConditional._else();// 获得else的模块体

		JClass sys = codeMode.ref("java.lang.System");
		JFieldRef out = sys.staticRef("out");
		ifBody.invoke(out, "println").arg(JExpr.ref("datas[i]"));

		elseBody._continue();
	}

	/**
	 * 生成代码
	 * 
	 * <pre>
	 * public void fun() {
	 * 	int[] datas = new int[] { 5, 6, 7, 2, 8, 9 };
	 * 	int bottom = 7;
	 * 	filter(datas, bottom);
	 * }
	 * </pre>
	 * 
	 * @param codeMode
	 * @param genClass
	 */
	private static void genFunMethod(JCodeModel codeMode, JDefinedClass genClass)
			throws Exception {
		JMethod funMehtod = genClass.method(JMod.PUBLIC,
				codeMode.parseType("void"), "fun");// 生成返回值类型为void的fun方法
		JBlock methodBody = funMehtod.body();
		// 生成局部变量int []datas;
		JVar datasVar = methodBody.decl(codeMode.parseType("int []"), "datas");
		JArray initArray = JExpr.newArray(codeMode.INT); // 创建类型为整型的数组
		initArray.add(JExpr.lit(5));// 添加数据
		initArray.add(JExpr.lit(6));
		initArray.add(JExpr.lit(2));
		initArray.add(JExpr.lit(8));
		initArray.add(JExpr.lit(9));
		methodBody.assign(datasVar, initArray);// 在方法体中，对datasVar进行赋值

		JVar bottomVar = methodBody.decl(codeMode.parseType("int"), "bottom",
				JExpr.lit(7));// 生成初始值为7的int局部变量bottom

		JInvocation invocation = methodBody.invoke("filter");// 调用filter方法
		invocation.arg(datasVar).arg(bottomVar);// 依次传递参数 datasVar和bottomVar
	}

	/**
	 * 生成代码
	 * 
	 * <pre>
	 * public synchronized static Utils getInstance() {
	 * 	if (sUtils == null) {
	 * 		sUtils = new Utils();
	 * 	}
	 * 	return sUtils;
	 * }
	 * </pre>
	 * 
	 * @param codeMode
	 * @param genClass
	 */
	private static void genGetInstanceMethod(JCodeModel codeMode,
			JDefinedClass genClass, JFieldVar sUtilsField) throws Exception {
		JType utilsType = codeMode.parseType("Utils");// 生成一个名为Utils的类型
		JMethod jMethod = genClass.method(JMod.PUBLIC + JMod.SYNCHRONIZED
				+ JMod.STATIC, utilsType, "getInstance");// 生成返回值Utils的名为getInstance的私有静态方法
		JBlock methodBody = jMethod.body();// 获得方法体
		JConditional ifConditional = methodBody._if(JExpr.ref("sUtils").eq(
				JExpr.ref("null")));// 生成if(sUtils == null)条件语句
		JBlock ifBody = ifConditional._then();// 获得条件语句的方法块
		ifBody.assign(sUtilsField, JExpr._new(utilsType));// 对sUtilsField进行赋值，新建一个Utils
		methodBody._return(sUtilsField);// 将成员变量sUtilsField返回
	}

	/**
	 * 生成代码
	 * 
	 * <pre>
	 * private Utils() {
	 * 	super();
	 * }
	 * </pre>
	 * 
	 * @param codeMode
	 * @param genClass
	 */
	private static void genPrivateConstructor(JCodeModel codeMode,
			JDefinedClass genClass) {
		JMethod constructor = genClass.constructor(JMod.PRIVATE);// 生成构造方法
		JBlock blk = constructor.body();// 获取方法体
		blk.invoke("super");// 在方法体中调用super方法
	}

	/**
	 * 生成代码：
	 * 
	 * <pre>
	 * private static Utils sUtils = null;
	 * </pre>
	 * 
	 * @param codeMode
	 * @param genClass
	 * @return
	 * @throws Exception
	 */
	private static JFieldVar genStaticField(JCodeModel codeMode,
			JDefinedClass genClass) throws Exception {
		JType utilsType = codeMode.parseType("Utils");// 生成一个名为Utils的类型
		JFieldVar sUtilsField = genClass.field(JMod.PRIVATE + JMod.STATIC,
				utilsType, "sUtils", JExpr._null());// 生成一个私有的静态成员变量,并赋值为null
		return sUtilsField;
	}

    }

</code>

生成的对应代码如下：
<code>
	
    package net.mobctrl.aa;

    import java.io.Serializable;

    public class Utils
    implements Serializable{

    private static Utils sUtils = null;

    private Utils() {
        super();
    }

    public static synchronized Utils getInstance() {
        if (sUtils == null) {
            sUtils = new Utils();
        }
        return sUtils;
    }

    public void fun() {
        int[] datas;
        datas = new int[] { 5, 6, 2, 8, 9 };
        int bottom = 7;
        filter(datas, bottom);
    }

    /**
     * doc: filter datas
     * 
     */
    private void filter(int[] datas, int bottom) {
        for (int i = 0; (i<datas.length); i ++) {
            if (datas[i]<bottom) {
                System.out.println(datas[i]);
            } else {
                continue;
            }
        }
    }

    }

</code>

##参考：<br>
[1] [用 Java 生成 Java - CodeModel 介绍](https://www.ibm.com/developerworks/cn/java/j-lo-codemodel/)<br>
[2] [官方文档](https://codemodel.java.net/nonav/apidocs/com/sun/codemodel/package-summary.html)
