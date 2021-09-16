package McLauncher.Utils.Event;

import java.util.ArrayList;
import java.util.List;

public abstract  class Observable {
    private List<Observer> observers = new ArrayList<Observer>();

    public void addObserver(Observer observer){
        observers.add(observer);		
     }
  
     public void change(){
        for (Observer observer : observers) {
           observer.update(this);
        }
     } 	
}
