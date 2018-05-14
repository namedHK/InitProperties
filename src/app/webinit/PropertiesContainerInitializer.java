package app.webinit;


import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 *在项目启动时添加一些系统配置参数
 * 实现方法，集成 ServletContainerInitializer
 * 在META-INF/services/javax.servlet.ServletContainerInitializer指定实现类
 * 重写onStartup方法
 * @HandlesTypes 可以指定一些特定的类作为启动方法的参数
 * @author hk
 */
@HandlesTypes(WebInit.class)
public class PropertiesContainerInitializer implements ServletContainerInitializer {

	@Override
	public void onStartup(Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		List<WebInit> initializers = new LinkedList<WebInit>();

		if (webAppInitializerClasses != null) {
			for (Class<?> waiClass : webAppInitializerClasses) {
				// Be defensive: Some servlet containers provide us with invalid classes,
				// no matter what @HandlesTypes says...
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
                        WebInit.class.isAssignableFrom(waiClass)) {
					try {
						initializers.add((WebInit) waiClass.newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
		for (WebInit initializer : initializers) {
			initializer.init(servletContext);
		}
	}

}
