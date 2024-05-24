public class ParentingAgeException extends Exception {

    public final Person person;
    private static String Lifespan(Person person) {
        return String.format("%s %s<->%s", person.getName(), person.getBirthDate(), person.getDeathDate() == null ? "" :  person.getDeathDate());
    }
    public ParentingAgeException(Person person, Person parent) {
        super(String.format("%s could not be a parent of %s.",Lifespan(parent), Lifespan(person)));
        this.person = person;
    }
}