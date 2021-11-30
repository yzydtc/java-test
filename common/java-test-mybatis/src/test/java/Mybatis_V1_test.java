import com.yzy.mybatis.chart2.SimpleTypeRegistry;
import com.yzy.mybatis.chart2.pojo.User;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/***
 * 使用properties解决硬编码
 */
public class Mybatis_V1_test {

    private Properties prop = new Properties();

    /**
     * 加载配置文件
     */
    private void loadProperties() {
        String jdbc_prop = "jdbc.properties";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(jdbc_prop);
        try {
            prop.load(ins);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试
     */
    @Test
    public void test()  {
        //获取配置文件
        loadProperties();
        //获取查询的数据集,简单参数类型
       /* List<User> list_simple = selectList("queryUserById","azx");
        System.out.println(list_simple);*/
        //获取查询的数据集,map参数类型
        Map<String,Object> params_map = new HashMap<String, Object>();
        params_map.put("name","zs");
        params_map.put("id",1);
        List<User> list_map = selectList("queryUserById",params_map);
        System.out.println(list_map);
    }


    private <T> List<T> selectList(String statementid, Object params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> resultList = new ArrayList<T>();
        try {
            //注册驱动
            Class.forName(prop.getProperty("db.driver"));
            //获取连接
            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.user");
            String password = prop.getProperty("db.password");
            conn = DriverManager.getConnection(url, user, password);
            //拼接sql
            String sql = prop.getProperty("db.sql."+statementid);
            //获取ps
            ps = conn.prepareStatement(sql);
            //设置参数
            if(SimpleTypeRegistry.isSimpleType(params.getClass())){
                ps.setObject(1,params);
            }else if(params instanceof Map){
                Map map = (Map)params;
                //获取参数集
                String params_prop = prop.getProperty("db.sql."+statementid+".params");
                String[] paramArray = params_prop.split(",");
                for(int i=0;i<paramArray.length;i++){
                    Object value = map.get(paramArray[i]);
                    ps.setObject(i+1,value);
                }
            }
            else{}
            //获取结果集
            rs = ps.executeQuery();

            //获取返回结果list的类型
            String resultclassname = prop.getProperty("db.sql."+statementid+".resultclassname");
            Class<?> reslultType = Class.forName(resultclassname);
            //返回结果
            while(rs.next()){
                Object result = reslultType.newInstance();
                //获取有多少列
                ResultSetMetaData resultMeta = rs.getMetaData();
                int columnCount = resultMeta.getColumnCount();

                for (int i = 1;i <= columnCount;i++){
                    //获取列名
                    String columnName = resultMeta.getColumnName(i);
                    Field field = reslultType.getDeclaredField(columnName);
                    field.setAccessible(true);
                    field.set(result,rs.getObject(columnName));
                }
                resultList.add((T) result);
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(rs != null){
                    rs.close();
                }
                if (ps != null){
                    ps.close();
                }
                if (conn !=null){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
