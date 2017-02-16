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
  doActivity(stud1,school.course3,"add")


  def doActivity(a:Student,b:Course,operation:String):Unit= {
    operation match {
      case add => a.addClass(b)
      case delete => a.removeClass(b)
    }
  }
 /* def courseActivity(c:Any,operation:String): Unit={
    operation match {
      case add => school.addCourse((Course) c)
      case delete => school.removeCourse((Course) c)
      case addStudent => school.addStudent((Student) c)
      case removeStudent => school.removeStudent((Student) c)
    }
  }*/


  school.addStudent(stud1)
  school.addStudent(stud2)
  school.addStudent(stud3)
println(school.getStudents())

}
case class School (schoolName: String, schoolAddress: String){

  val students = ArrayBuffer.empty[Student]
  //var students = List(stud1,stud2,stud3)

  val course1  = Course("CS",1,"ABC")
  val course2 = Course("EEE",2,"DEF")
  val course3 = Course("MECH",3,"GHI")
  var courses = List(course1,course2,course3)

  def getStudents():ArrayBuffer[Student] = {
    students
  }
  def getCourses():List[Course] = {
    courses
  }

  def addCourse(c:Course) = {
    courses = c::courses
  }

  def removeCourse(c:Course)={

  }
  def addStudent(s:Student) = {
    students+=s
    println(s)
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
    "studentName "+studentName+" courses "+classes+" ID "+studentId
  }
}

case class Course(courseName: String, courseId: Int, profName: String)
{

}