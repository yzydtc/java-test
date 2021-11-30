import com.mysql.jdbc.StringUtils;
import com.yzy.spring.chart1.ioc.BeanDefinition;
import com.yzy.spring.chart1.ioc.PropertyValue;
import com.yzy.spring.chart1.ioc.RuntimeBeanReference;
import com.yzy.spring.chart1.ioc.TypedStringValue;
import com.yzy.spring.chart1.pojo.User;
import com.yzy.spring.chart1.service.UserService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 面向过程实现ioc
 */
public class TestPOPSpring {
    //存储bean单例实体
    private HashMap<String,Object> singletonObjects=new HashMap<>();
    //存储bean单例实体 value:BeanDefinition
    private HashMap<String, BeanDefinition> beanDefinitions=new HashMap<>();

    @Before
    public void before() {
        // XML解析，解析的结果，放入beanDefinitions中
        String location = "beans.xml";
        // 获取流对象
        InputStream inputStream = getInputStream(location);
        // 创建文档对象
        Document document = createDocument(inputStream);

        // 按照spring定义的标签语义去解析Document文档
        parseBeanDefinitions(document.getRootElement());
    }
    private Document createDocument(InputStream inputStream) {
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(inputStream);
            return document;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }
    private InputStream getInputStream(String location) {
        return this.getClass().getClassLoader().getResourceAsStream(location);
    }
    @SuppressWarnings("unchecked")
    private void parseDefaultElement(Element beanElement) {
        try {
            if (beanElement == null)
                return;
            // 获取id属性
            String id = beanElement.attributeValue("id");

            // 获取name属性
            String name = beanElement.attributeValue("name");

            // 获取class属性
            String clazzName = beanElement.attributeValue("class");
            if (clazzName == null || "".equals(clazzName)) {
                return;
            }

            // 获取init-method属性
            String initMethod = beanElement.attributeValue("init-method");
            // 获取scope属性
            String scope = beanElement.attributeValue("scope");
            scope = scope != null && !scope.equals("") ? scope : "singleton";

            // 获取beanName
            String beanName = id == null ? name : id;
            Class<?> clazzType = Class.forName(clazzName);
            beanName = beanName == null ? clazzType.getSimpleName() : beanName;
            // 创建BeanDefinition对象
            // 此次可以使用构建者模式进行优化
            BeanDefinition beanDefinition = new BeanDefinition(clazzName, beanName);
            beanDefinition.setInitMethod(initMethod);
            beanDefinition.setScope(scope);
            // 获取property子标签集合
            List<Element> propertyElements = beanElement.elements();
            for (Element propertyElement : propertyElements) {
                parsePropertyElement(beanDefinition, propertyElement);
            }

            // 注册BeanDefinition信息
            this.beanDefinitions.put(beanName, beanDefinition);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void parsePropertyElement(BeanDefinition beanDefination, Element propertyElement) {
        if (propertyElement == null)
            return;

        // 获取name属性
        String name = propertyElement.attributeValue("name");
        // 获取value属性
        String value = propertyElement.attributeValue("value");
        // 获取ref属性
        String ref = propertyElement.attributeValue("ref");

        // 如果value和ref都有值，则返回
        if (value != null && !value.equals("") && ref != null && !ref.equals("")) {
            return;
        }

        /**
         * PropertyValue就封装着一个property标签的信息
         */
        PropertyValue pv = null;

        if (value != null && !value.equals("")) {
            // 因为spring配置文件中的value是String类型，而对象中的属性值是各种各样的，所以需要存储类型
            TypedStringValue typeStringValue = new TypedStringValue(value);

            Class<?> targetType = getTypeByFieldName(beanDefination.getClazzName(), name);
            typeStringValue.setTargetType(targetType);

            pv = new PropertyValue(name, typeStringValue);
            beanDefination.addPropertyValue(pv);
        } else if (ref != null && !ref.equals("")) {

            RuntimeBeanReference reference = new RuntimeBeanReference(ref);
            pv = new PropertyValue(name, reference);
            beanDefination.addPropertyValue(pv);
        } else {
            return;
        }
    }
    private Class<?> getTypeByFieldName(String beanClassName, String name) {
        try {
            Class<?> clazz = Class.forName(beanClassName);
            Field field = clazz.getDeclaredField(name);
            return field.getType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void parseCustomElement(Element element) {
    }
    @SuppressWarnings("unchecked")
    public void parseBeanDefinitions(Element rootElement) {
        // 获取<bean>和自定义标签（比如mvc:interceptors）
        List<Element> elements = rootElement.elements();
        for (Element element : elements) {
            // 获取标签名称
            String name = element.getName();
            if (name.equals("bean")) {
                // 解析默认标签，其实也就是bean标签
                parseDefaultElement(element);
            } else {
                // 解析自定义标签，比如aop:aspect标签
                parseCustomElement(element);
            }
        }
    }



    @Test
    public void test(){
        //创建bean
        UserService userService = (UserService) getBean("userService");
        //获取参数
        Map<String,Object> param = new HashMap<>();
        param.put("username","zs");
        //获取结果
        List<User> result_list = userService.queryUsers(param);
        System.out.println(result_list);
    }

    /*读取xml获取bean信息
    在bean创建时一次解析xml
    一般使用单例模式
    */

    private Object getBean(String beanName) {
        //根据beanName去singletonObjects查询结果，没有就创建
        Object bean = singletonObjects.get(beanName);
        if(bean != null) return bean;
        //创建
        BeanDefinition bd = beanDefinitions.get(beanName);
        //判断是否单例
        if(bd.isSingleton()){
            bean = createBean(bd);
            singletonObjects.put(beanName,bean);
        }else if(bd.isPrototype()){
            bean = createBean(bd);
        }else if(bd == null){
            bean = null;
        }
        return bean;
    }

    private Object createBean(BeanDefinition bd) {
        Class<?> clazzType = bd.getClazzType();
        if(clazzType == null){
            return null;
        }
        //Bean实例化
        Object bean = createBeanInstance(clazzType);
        //Bean的属性填充
        populateBean(bean,bd);
        //Bean初始化
        initializeBean(bean,bd);
        return bean;
    }

    private Object createBeanInstance(Class<?> clazzType) {
        try {
            Constructor<?> constructor = clazzType.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void populateBean(Object bean, BeanDefinition bd) {
        List<PropertyValue> list = bd.getPropertyValues();
        for(PropertyValue pv : list){
            String name = pv.getName();
            Object value = pv.getValue();
            //处理参数
            Object param = resolveValue(value);
            setProperty(bean, name, param, bd);
        }
    }

    private void setProperty(Object bean, String name, Object param, BeanDefinition bd) {
        Class<?> clazzType = bd.getClazzType();
        Field field = null;
        try {
            field = clazzType.getDeclaredField(name);
            field.setAccessible(true);
            field.set(bean,param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object resolveValue(Object value) {
       if(value instanceof TypedStringValue){
           TypedStringValue tsv = (TypedStringValue)value;
           String stringValue = tsv.getValue();
           Class<?> targetType = tsv.getTargetType();
           if(targetType == Integer.class){
               return Integer.parseInt(stringValue);
           }else if(targetType == String.class){
               return stringValue;
           }
       }else if(value instanceof RuntimeBeanReference){
           RuntimeBeanReference rbr = (RuntimeBeanReference)value;
           String rbf = rbr.getRef();
           return getBean(rbf);
       }
       return null;
    }

    private void initializeBean(Object bean, BeanDefinition bd) {
        //TODO aware接口实现
        //TODO 处理InitializingBean的初始化

        //处理办法
        invokeInitMethod(bean,bd);
    }

    private void invokeInitMethod(Object bean, BeanDefinition bd) {
        String initMethod = bd.getInitMethod();
        if(StringUtils.isNullOrEmpty(initMethod)){
            return;
        }
        Class<?> clazzType = bd.getClazzType();
        try {
            Method method = clazzType.getMethod(initMethod);
            method.invoke(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
