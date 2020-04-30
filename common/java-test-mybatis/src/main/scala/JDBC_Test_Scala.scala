import java.sql.{Connection, DriverManager, ResultSet, SQLException};

object JDBC_Test_Scala {
  def main(args: Array[String]): Unit = {
    //注册驱动
    classOf[com.mysql.jdbc.Driver]
    //获取连接
    val conn = DriverManager.getConnection("jdbc:mysql://cluster2:3306/java_test?characterEncoding=utf8",
      "root", "!Qaz123456")
    //编写sql
    val sql = "select * from jdbc_test"
    try{
    //获取stateMent
    val ps = conn.prepareStatement(sql)
    //ps.setInt(1,1)
    //获取结果集
    val rs = ps.executeQuery()
    while(rs.next()){
      var id = rs.getInt(1)
      var name = rs.getString(2)
      println("id:"+id+" "+name)
      }
    }finally {
      //释放资源
      conn.close()
    }
  }
}
