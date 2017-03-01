import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by lokesh0973 on 2/14/2017.
  */
object SchoolApp extends App {


  val school = School("Test School", "Dalls,TX")

  val stud1 = Student("AAA",111)
  val stud2 = Student("BBB",222)
  val stud3 = Student("CCC",444)

  val course1  = Course("CS",1,"ABC")
  val course2 = Course("EEE",2,"DEF")
  val course3 = Course("MECH",3,"GHI")

  doActivity(stud1,course3,"add")




  def doActivity(a:Student,b:Course,operation:String):Unit= {
    operation match {
      case "add" => a.addClass(b)
      case "delete" => a.removeClass(b)
    }
  }
  def courseActivity(c:Course,operation:String): Unit={
    operation match {
      case "add" => school.addCourse(c)
      case "delete" => school.removeCourse(c)
    }
  }


  school.addStudent(stud1)
  school.addStudent(stud2)
  school.addStudent(stud3)
println(school.getStudents())

}
case class School (schoolName: String, schoolAddress: String){

  val students = ArrayBuffer.empty[Student]
  //var students = List(stud1,stud2,stud3)

  var courses = ArrayBuffer.empty[Course]

  def getStudents():ArrayBuffer[Student] = {
    students
  }
  def getCourses():ArrayBuffer[Course] = {
    courses
  }

  def addCourse(c:Course) = {
    courses+=c
  }

  def removeCourse(c:Course)={
    courses-=c
  }
  def addStudent(s:Student) = {
    students+=s
  }

  def removeStudent(s:Student)={
    students-=s
  }
}

case class Student(studentName: String, studentId: Int)
{
 val classes = ArrayBuffer.empty[Int]
  def addClass(c:Course) = {
    classes+= c.courseId
  }
  def removeClass(c:Course) = {

  }

  override def toString: String = {
    "Student("+studentName+",  ID "+studentId+","+classes+")"
  }
}

case class Course(courseName: String, courseId: Int, profName: String)
{
  override def toString: String = {
    "Course("+courseName+", "+courseId+","+profName+")"
  }
}