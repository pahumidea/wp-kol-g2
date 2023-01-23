package mk.ukim.finki.wp.kol2022.g2.service.impl;

import mk.ukim.finki.wp.kol2022.g2.model.Course;
import mk.ukim.finki.wp.kol2022.g2.model.Student;
import mk.ukim.finki.wp.kol2022.g2.model.StudentType;
import mk.ukim.finki.wp.kol2022.g2.model.exceptions.InvalidCourseIdException;
import mk.ukim.finki.wp.kol2022.g2.model.exceptions.InvalidStudentIdException;
import mk.ukim.finki.wp.kol2022.g2.repository.CourseRepository;
import mk.ukim.finki.wp.kol2022.g2.repository.StudentRepository;
import mk.ukim.finki.wp.kol2022.g2.service.StudentService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository, CourseRepository courseRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Student> listAll() {
        return studentRepository.findAll();
    }

    @Override
    public Student findById(Long id) {
        return studentRepository.findById(id).orElseThrow(InvalidStudentIdException::new);
    }

    @Override
    public Student create(String name, String email, String password, StudentType type, List<Long> courseId, LocalDate enrollmentDate) {
        List<Course> courseList = courseRepository.findAllById(courseId);
        if (courseList.isEmpty()) throw new InvalidCourseIdException();
        return studentRepository.save(new Student(name, email, passwordEncoder.encode(password), type, courseList, enrollmentDate));
    }

    @Override
    public Student update(Long id, String name, String email, String password, StudentType type, List<Long> coursesId, LocalDate enrollmentDate) {
        List<Course> courseList = courseRepository.findAllById(coursesId);
        if (courseList.isEmpty()) throw new InvalidCourseIdException();
        Student oldStudent = studentRepository.findById(id).orElseThrow(InvalidStudentIdException::new);
        oldStudent.setName(name);
        oldStudent.setEmail(email);
        oldStudent.setPassword(passwordEncoder.encode(password));
        oldStudent.setType(type);
        oldStudent.setCourses(courseList);
        oldStudent.setEnrollmentDate(enrollmentDate);
        return studentRepository.save(oldStudent);
    }

    @Override
    public Student delete(Long id) {
        Student oldStudent = studentRepository.findById(id).orElseThrow(InvalidStudentIdException::new);
        studentRepository.delete(oldStudent);
        return oldStudent;
    }

    @Override
    public List<Student> filter(Long courseId, Integer yearsOfStudying) {
        List<Student> results;
        if (courseId == null && yearsOfStudying == null) {
            results = studentRepository.findAll();
        } else if (courseId != null && yearsOfStudying != null) {
            results = studentRepository.findAllByCoursesContainingAndEnrollmentDateBefore(
                    courseRepository.findById(courseId).orElseThrow(InvalidStudentIdException::new),
                    LocalDate.now().minusYears(yearsOfStudying)
            );
        } else if (courseId != null) {
            results = studentRepository.findAllByCoursesContaining(courseRepository.findById(courseId).orElseThrow(InvalidStudentIdException::new));
        } else {
            results = studentRepository.findAllByEnrollmentDateBefore(LocalDate.now().minusYears(yearsOfStudying));
        }

        return results;
    }
}
