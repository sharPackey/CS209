import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(
                    new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1],
                        new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]),
                        Integer.parseInt(info[8]), Integer.parseInt(info[9]),
                        Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                        Double.parseDouble(info[14]), Double.parseDouble(info[15]),
                        Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                        Double.parseDouble(info[20]), Double.parseDouble(info[21]),
                        Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        List<Course> list1 = new ArrayList<>(courses);
        return list1.stream().collect(
                Collectors.groupingBy(Course::getInstitution,
                        TreeMap::new,
                        Collectors.summingInt(Course::getParticipants)));
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        List<Course> cc = new ArrayList<>(courses);
        Map<String, String> qnmd = new HashMap<>();
        for (int i = 0; i < cc.size(); i++) {
            cc.get(i).gg = cc.get(i).institution + "-" + cc.get(i).subject;
        }
        Map<String, Integer> map2 = cc.stream().collect(
                Collectors.groupingBy(Course::getGg,
                        Collectors.summingInt(Course::getParticipants)));
        return sortMap(map2);
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {

        Map<String, List<List<String>>> map3 = new HashMap<>();

        for (Course cours : courses) {
            String[] name = cours.instructors.split(", ");
            int ff = 0;
            if (name.length > 1) {
                ff = 1;
            }
            for (String s : name) {
                if (!map3.containsKey(s)) {
                    List<List<String>> o = new ArrayList<>();
                    List<String> p = new ArrayList<>();
                    List<String> w = new ArrayList<>();
                    o.add(p);
                    o.add(w);
                    map3.put(s, o);
                }
                if (ff == 1) {
                    if (!map3.get(s).get(1).contains(cours.title)) {
                        map3.get(s).get(1).add(cours.title);
                    }
                } else {
                    if (!map3.get(s).get(0).contains(cours.title)) {
                        map3.get(s).get(0).add(cours.title);
                    }
                }
            }
        }
        for (List<List<String>> ss : map3.values()) {
            ss.get(0).sort(String::compareTo);
            ss.get(1).sort(String::compareTo);
        }
        return map3;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<Course> cc = new ArrayList<>(courses);
        if (by.equals("hours")) {
            cc = cc.stream().sorted(
                    Comparator.comparing(Course::getTotalHours)
                            .reversed()).toList();
        } else {
            cc = cc.stream().sorted(
                    Comparator.comparing(Course::getParticipants)
                            .reversed()).toList();
        }
        List<String> list4 = new ArrayList<>();
        int k = 0;
        int real = 0;
        while (real < topK) {
            if (!list4.contains(cc.get(k).title)) {
                list4.add(cc.get(k).title);
                real++;
            }
            k++;
        }
        return list4;
    }
    //5
    public List<String> searchCourses(String courseSubject,
                                      double percentAudited, double totalCourseHours) {
        List<Course> list5 = new ArrayList<>(courses);
        return list5.stream()
                .filter(v -> v.getSubject()
                        .toLowerCase().contains((courseSubject.toLowerCase())))
                .filter(u -> u.getPercentAudited() >= percentAudited)
                .filter(h -> h.getTotalHours() <= totalCourseHours)
                .map(Course::getTitle)
                .distinct().sorted(String::compareTo).toList();
    }

    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, Double> numberage = courses.stream().collect(
                Collectors.groupingBy(Course::getNumber,
                        Collectors.averagingDouble(Course::getMedianAge))
        );
        Map<String, Double> numberMale = courses.stream().collect(
                Collectors.groupingBy(Course::getNumber,
                        Collectors.averagingDouble(Course::getPercentMale))
        );
        Map<String, Double> numberbachelor = courses.stream().collect(
                Collectors.groupingBy(Course::getNumber,
                        Collectors.averagingDouble(Course::getPercentDegree))
        );
        Map<String, Double> Titleval = new TreeMap<>();
        Map<String, String> numberTitle = new HashMap<>();
        Map<String, List<Course>> numberCourse;
        numberCourse = courses.stream()
                .collect(Collectors.groupingBy(Course::getNumber));
        numberCourse.forEach((s, courses) -> {
            courses.sort((o1, o2) -> o2.getLaunchDate().compareTo(o1.getLaunchDate()));
        });
        numberCourse.forEach((s, courses) -> {
            numberTitle.put(s, courses.get(0).getTitle());
        });
        numberCourse.keySet().forEach(s -> {
            Titleval.put(numberTitle.get(s),
                    Math.pow(age - numberage.get(s), 2)
                            + Math.pow(gender * 100 - numberMale.get(s), 2)
                            + Math.pow(isBachelorOrHigher * 100 - numberbachelor.get(s), 2));
        });
        List<String> ans = new ArrayList<>();
        List<String> finalans = ans;
        Titleval.entrySet().stream().sorted((o1, o2) -> {
            int ansult = o1.getValue().compareTo(o2.getValue());
            if (ansult == 0) {
                return o1.getKey().compareTo(o2.getKey());
            } else {
                return ansult;
            }
        }).forEach(S -> {
            if (!finalans.contains(S.getKey())) {
                finalans.add(S.getKey());
            }
        });
        ans = finalans.stream().limit(10).toList();
        return ans;
    }

    public static Map<String, Integer> sortMap(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> e : entryList
        ) {
            linkedHashMap.put(e.getKey(), e.getValue());
        }
        return linkedHashMap;
    }
}


class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;

    String gg;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Date getLaunchDate() {
        return launchDate;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public String getGg() {
        return gg;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public String getInstitution() {
        return institution;
    }

    public String getNumber() {
        return number;
    }

    public int getParticipants() {
        return participants;
    }

    public String getInstructors() {
        return instructors;
    }

    public String getSubject() {
        return subject;
    }

    public String getTitle() {
        return title;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}
