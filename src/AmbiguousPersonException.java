public class AmbiguousPersonException extends Exception{
    public AmbiguousPersonException(Person person){
        super(String.format("%s appears in file more than one time.",person.getName()));
    }
}
