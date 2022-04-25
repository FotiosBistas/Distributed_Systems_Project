import java.io.IOException;

class Shared_Network_Methods{


    /**
     * Accepts the object that is requesting for the broker list.
     * After that it sends the broker list using the appropriate streams and sockets.
     * @param caller
     */
    public static void sendBrokerList(Object caller){
        System.out.println("Sending broker list");
        if(caller instanceof Broker){
            Broker broker = (Broker) caller;
            try {
                System.out.println("Sending message type: " + Messages.SENDING_BROKER_LIST + " with ordinal number: " + Messages.SENDING_BROKER_LIST.ordinal());
                broker.getLocaloutputStream().writeInt(Messages.SENDING_BROKER_LIST.ordinal());
                broker.getLocaloutputStream().flush();
                for (Tuple<String, int[]> val : broker.getBrokerList()) {
                    System.out.println("Sending Broker List size: " + broker.getBrokerList().size());
                    broker.getLocaloutputStream().writeInt(broker.getBrokerList().size());
                    broker.getLocaloutputStream().flush();
                    System.out.println("Sending broker's IP: " + val.getValue1());
                    broker.getLocaloutputStream().writeUTF(val.getValue1());
                    broker.getLocaloutputStream().flush();
                    int i;
                    for (i = 0; i < val.getValue2().length; i++) {
                        System.out.println("Sending broker's ports: " + val.getValue2()[i]);
                        broker.getLocaloutputStream().writeInt(val.getValue2()[i]);
                        broker.getLocaloutputStream().flush();
                    }
                    if (i == 3) {
                        System.out.println("Finished sending ports");
                        broker.FinishedOperation();
                    }
                }
                System.out.println("Finished sending broker list");
            }
            catch (IOException e){

            }
        }else if(caller instanceof Consumer_Handler){
            Consumer_Handler handler = (Consumer_Handler) caller;
            try{
                System.out.println("Sending message type: " + Messages.SENDING_BROKER_LIST + " with ordinal number: " + Messages.SENDING_BROKER_LIST.ordinal());
                handler.getLocaloutputStream().writeInt(Messages.SENDING_BROKER_LIST.ordinal());
                handler.getLocaloutputStream().flush();
                for (Tuple<String, int[]> val : handler.getBroker().getBrokerList()) {
                    System.out.println("Sending Broker List size: " + handler.getBroker().getBrokerList().size());
                    handler.getLocaloutputStream().writeInt(handler.getBroker().getBrokerList().size());
                    handler.getLocaloutputStream().flush();
                    System.out.println("Sending broker's IP: " + val.getValue1());
                    handler.getLocaloutputStream().writeUTF(val.getValue1());
                    handler.getLocaloutputStream().flush();
                    int i;
                    for (i = 0; i < val.getValue2().length; i++) {
                        System.out.println("Sending broker's ports: " + val.getValue2()[i]);
                        handler.getLocaloutputStream().writeInt(val.getValue2()[i]);
                        handler.getLocaloutputStream().flush();
                    }
                    if (i == 3) {
                        System.out.println("Finished sending ports");
                        handler.FinishedOperation();
                    }
                }
            }
            catch (IOException e){
                e.printStackTrace();
                System.out.println("Shutting down connection in send broker list...");
                handler.shutdownConnection();
            }
        }
    }

    /**
     * Accepts the object that is requesting for the broker list.
     * After that it sends the broker list using the appropriate streams and sockets.
     * @param caller
     */
    public static void sendIdList(Object caller){
        System.out.println("Sending ID List");
        if(caller instanceof Broker) {
            Broker broker = (Broker) caller;
            try {
                System.out.println("Sending message type: " + Messages.SENDING_ID_LIST + " with ordinal number: " + Messages.SENDING_ID_LIST.ordinal());
                broker.getLocaloutputStream().writeInt(Messages.SENDING_ID_LIST.ordinal());
                broker.getLocaloutputStream().flush();
                for (int i = 0; i < broker.getId_list().size(); i++) {
                    System.out.println("Sending ID List size: " + broker.getId_list().size());
                    broker.getLocaloutputStream().writeInt(broker.getId_list().size());
                    broker.getLocaloutputStream().flush();
                    System.out.println("Sending ID: " + broker.getId_list().get(i));
                    broker.getLocaloutputStream().writeInt(broker.getId_list().get(i));
                    broker.getLocaloutputStream().flush();
                }
                System.out.println("Finished sending ID list");
            } catch (IOException e) {
                System.out.println("Error in sending list");
                e.printStackTrace();
                broker.shutdownConnection();
            }
        }
        else if(caller instanceof Consumer_Handler){
            Consumer_Handler handler = (Consumer_Handler) caller;
            try {
                System.out.println("Sending message type: " + Messages.SENDING_ID_LIST + " with ordinal number: " + Messages.SENDING_ID_LIST.ordinal());
                handler.getLocaloutputStream().writeInt(Messages.SENDING_ID_LIST.ordinal());
                handler.getLocaloutputStream().flush();
                for (int i = 0; i < handler.getBroker().getId_list().size(); i++) {
                    System.out.println("Sending ID List size: " + handler.getBroker().getId_list().size());
                    handler.getLocaloutputStream().writeInt(handler.getBroker().getId_list().size());
                    handler.getLocaloutputStream().flush();
                    System.out.println("Sending ID: " + handler.getBroker().getId_list().get(i));
                    handler.getLocaloutputStream().writeInt(handler.getBroker().getId_list().get(i));
                    handler.getLocaloutputStream().flush();
                }
            } catch (IOException e) {
                System.out.println("Error in sending list");
                e.printStackTrace();
                handler.shutdownConnection();
            }
        }
    }

    public static void sendTopicList(){

    }

}
