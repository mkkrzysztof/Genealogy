import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Person implements Serializable {
    private final String name;
    private final LocalDate birthDate;
    private final LocalDate deathDate;
    private final List<Person> parents;

    public Person(String name, LocalDate birthDate, LocalDate deathDate){
        this.name = name;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.parents = new ArrayList<>();
    }
    public void addParent(Person person){
        parents.add(person);
    }
    public static Person fromCsvLine(String line){
        String[] parts = line.split(",", -1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate birthDate = LocalDate.parse(parts[1], formatter);
        LocalDate deathDate = (parts[2].isEmpty()) ? null : LocalDate.parse(parts[2], formatter);
        return new Person(parts[0], birthDate, deathDate);
    }
    public static List<Person> fromCsv(String path){
        List<Person> personList = new ArrayList<>();
        Map<String, PersonWithParentsNames> personWithParentsNamesMap = new HashMap<>();
        String line;
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        try{
            fileReader = new FileReader(path);
            bufferedReader = new BufferedReader(fileReader);
            bufferedReader.readLine();
            while((line = bufferedReader.readLine()) != null){
                PersonWithParentsNames personWithParentsNames = PersonWithParentsNames.fromCsvLine(line);
                Person person = personWithParentsNames.getPerson();
                try{
                    person.lifespanValidate();
                    person.ambiguityValidate(personList);
                    personList.add(person);
                    personWithParentsNamesMap.put(person.name, personWithParentsNames);

                }catch (NegativeLifespanException | AmbiguousPersonException e){
                    System.err.println(e.getMessage());
                }
            }
            PersonWithParentsNames.fillParents(personWithParentsNamesMap);
            try {
                for(Person person: personList) {
                    person.validateParentingAge();
                }
            }
            catch(ParentingAgeException exception) {
                Scanner scanner = new Scanner(System.in);
                System.out.println(exception.getMessage());
                System.out.println("Continue?: [Y/n]:");
                String response = scanner.nextLine();
                if(response.toLowerCase(Locale.ENGLISH).equals("n"))
                    personList.remove(exception.person);
            }
        } catch (IOException e){
            System.err.println(e.getMessage());
        } finally {
            if(bufferedReader != null){
                try{
                    bufferedReader.close();
                }catch (IOException e){
                    System.out.println(e.getMessage());
                }
            }
        }
        return personList;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public List<Person> getParents() {
        return parents;
    }

    public static void toBinaryFile(List<Person> people, String path){
        try (
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                ){
            oos.writeObject(people);
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
    public static List<Person> fromBinaryFile(String path){
        try(
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))
                ) {
            return (List<Person>)ois.readObject();
        }catch(IOException | ClassNotFoundException e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    public String generateUMLForOne() {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        Function<Person,String> deleteSpaces = person -> person.getName().replaceAll(" ","");
        Function<Person,String> addObject = person -> "object " + deleteSpaces.apply(person);
        sb.append(addObject.apply(this)).append("\n");
        parents.forEach(p -> {
            sb.append(addObject.apply(p)).append("\n");
            sb.append(String.format("%s <-- %s\n", deleteSpaces.apply(p), deleteSpaces.apply(this)));
        });
        sb.append("@enduml\n");
        return sb.toString();
    }
    public static String generateUmlWithPredicate(List<Person> people, Function<String, String> postProcess, Predicate<Person> condition) {
        Function<Person,String> deleteSpaces = p -> p.getName().replaceAll(" ","");
        Function<Person, String> addObject = person -> String.format("object %s\n", deleteSpaces.apply(person));
        Function<Person, String> postPr = addObject.andThen(postProcess);

        String objects = people.stream()
                .map(person -> condition.test(person)?postPr
                        .apply(person):addObject.apply(person) )
                .collect(Collectors.joining());

        String relationships = people.stream()
                .flatMap(person -> person.getParents().isEmpty() ?  Stream.empty():
                                person.getParents().stream().map(parent -> String.format("%s <-- %s\n",deleteSpaces.apply(parent),
                                                deleteSpaces.apply(person))))
                .collect(Collectors.joining());

        return String.format("@startuml\n%s%s@enduml", objects, relationships);
    }
    public static String generateUML(List<Person> people)
    {
        StringBuilder sb = new StringBuilder();
        Function<Person,String> deleteSpaces = p -> p.getName().replaceAll(" ","");
        Function<Person,String>  addObject = p -> "object " + deleteSpaces.apply(p);
        sb.append("@startuml");
        sb.append(people.stream()
                .map(p -> "\n" + addObject.apply(p))
                .collect(Collectors.joining()));
        sb.append(people.stream()
                .flatMap(person -> person.parents.isEmpty() ? Stream.empty() :
                        person.parents.stream()
                                .map(p -> "\n" + deleteSpaces.apply(p) + " <-- " + deleteSpaces.apply(person))).collect(Collectors.joining()));
        sb.append("\n@enduml");
        return sb.toString();
    }
    public static List<Person> filterByName(List<Person> people, String substring){
        return people.stream()
                .filter(person -> person.getName().contains(substring))
                .collect(Collectors.toList());
    }
    public static List<Person> sortedByBirth(List<Person> people){
        return people.stream()
                .sorted(Comparator.comparing(Person::getBirthDate))
                .collect(Collectors.toList());
    }

    public static List<Person> sortByLifespan(List<Person> people){

        Function<Person, Long> getLifespan = person
                -> person.deathDate.toEpochDay() - person.birthDate.toEpochDay();

        return people.stream()
                .filter(person -> person.deathDate != null)
                .sorted((o2, o1) -> Long.compare(getLifespan.apply(o1), getLifespan.apply(o2)))
                .toList();
    }

    public static Person OldestLiving(List<Person> people){
        return people.stream()
                .filter(person -> person.deathDate == null)
                .min(Comparator.comparing(Person::getBirthDate))
                .orElse(null);

    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", deathDate=" + deathDate +
                ", parents=" + parents +
                '}';
    }
    public void lifespanValidate() throws NegativeLifespanException{
        if(this.deathDate != null && this.deathDate.isBefore(this.birthDate)){
            throw new NegativeLifespanException(this);
        }
    }
    public void ambiguityValidate(List<Person> people) throws AmbiguousPersonException{
        for(Person i: people){
            if(Objects.equals(i.getName(), this.getName())){
                throw new AmbiguousPersonException(i);
            }
        }
    }
    private void validateParentingAge() throws ParentingAgeException {
        for(Person parent: parents) {
            if (birthDate.isBefore(parent.birthDate.plusYears(15)) || (parent.deathDate != null && birthDate.isAfter(parent.deathDate)))
                throw new ParentingAgeException(this, parent);
        }
    }
}
