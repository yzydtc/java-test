import java.sql.*;

public class JDBC_Test {

    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //注册驱动
            Class.forName("com.mysql.jdbc.Driver");
            //获取连接
            conn = DriverManager.getConnection("jdbc:mysql://cluster2:3306/java_test?characterEncoding=utf8",
                    "root","!Qaz123456");
            //拼接sql
            String sql = "select * from jdbc_test";
            //获取statment
            ps = conn.prepareStatement(sql);
            //conn.prepareCall()
            //ps.setInt(1,1);
            //获取结果集
            rs = ps.executeQuery();
            while (rs.next()){
                int id = rs.getInt(1);
                String name = rs.getString(2);
                System.out.println("id:"+id+"  "+"name:"+name);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }finally {
            //释放资源
            try{
                if(ps !=null) ps.close();
                ps=null;
                if(rs !=null) rs.close();
                rs=null;
                if(conn !=null) conn.close();
                conn=null;
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }
}
