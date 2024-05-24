public class NegativeLifespanException extends Exception{
    public NegativeLifespanException(Person person){
        super(String.format("%s died on %s before his/her birth %s",person.getName(),person.getDeathDate(),person.getBirthDate()));
    }
}
