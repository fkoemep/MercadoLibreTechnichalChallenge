package cloudcode.exceptions;

public class MessageProcessingException extends Exception{
    public MessageProcessingException(String errormsg){
        super(errormsg);
    }
}
