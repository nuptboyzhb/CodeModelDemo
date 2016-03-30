package net.mobctrl.aa.codemodel;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JArray;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * @Author Zheng Haibo (mochuan)
 * @Company Alibaba Group
 * @PersonalWebsite http://www.mobctrl.net
 * @version $Id: GenUtilsDemo.java, v 0.1 2016年3月29日 下午5:20:12 mochuan.zhb Exp $
 * @Description 生成一个工具类
 */
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
