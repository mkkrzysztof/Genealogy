import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Main {
    public static void main(String[] args) {
        List<Person> people = new ArrayList<>();
        people = Person.fromCsv("family.csv");
        PlantUMLRunner.setPath("/home/krzysztof/IdeaProjects/Genealogy/plantuml-1.2024.4.jar");

        String s = Person.generateUML(people);
        PlantUMLRunner.generateDiagram(s,"/home/krzysztof/IdeaProjects/Genealogy","diagram");

        s = people.get(3).generateUMLForOne();
        PlantUMLRunner.generateDiagram(s,"/home/krzysztof/IdeaProjects/Genealogy","diagram2");
//        Person.toBinaryFile(people,"people.bin");
//        people = Person.fromBinaryFile("people.bin");
//        for(Person i : people){
//            System.out.println(i.toString());
//        }

//        Person.filterByName(people, "Kowalsk").forEach(System.out::println);
//        System.out.println();
//        Person.sortedByBirth(people).forEach(System.out::println);
//        System.out.println();
//        Person.sortByLifespan(people).forEach(System.out::println);
//        System.out.println();
        Function<String, String> nothing = Function.identity();
        Function<String, String> colorYellow = x -> x.contains("object")?x.trim()+" #Yellow \n":x;

//        s = Person.generateUmlWithPostProcessing(people,colorYellow);
//        PlantUMLRunner.generateDiagram(s,"/home/krzysztof/IdeaProjects/Genealogy","diagram3");

        Predicate<Person> contains = x -> x.getName().contains("Kowalsk");
        s = Person.generateUmlWithPredicate(people,colorYellow,contains);
        PlantUMLRunner.generateDiagram(s,"/home/krzysztof/IdeaProjects/Genealogy","diagram3");
    }
}