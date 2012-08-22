package Tests;

import java.awt.event.ActionListener;  
import java.awt.event.ActionEvent;  
import javax.swing.JFrame;  
import javax.swing.BoxLayout;  
import javax.swing.JPanel;  
import javax.swing.JButton;  
import javax.swing.JLabel;  
import javax.swing.JTextArea;  
   
public class Testy extends JFrame implements ActionListener  
{  
  
  JPanel testPanel = new JPanel();          
  
  public Testy()  
  {  
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
setSize(300,400);  
setTitle("Practice GUI");  
getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));  
JButton HitMe = new JButton("Hit Me!");  
HitMe.addActionListener(new TestListener());  
getContentPane().add(HitMe);  
getContentPane().add(testPanel);  
setVisible(true);  
  }  
  
  public void actionPerformed(ActionEvent e)  
  {  
  }  
  
  class TestListener implements ActionListener  
  {  
public void actionPerformed(ActionEvent e)  
        {  
          //testPanel.add(new JButton("Blah"));  //<< This works  
          testPanel.add(new JLabel("Yeah!"));  //<< This works  
   
         // JTextArea jta = new JTextArea();  //<< This doesn't works.  How come?  
  //testPanel.add(jta);  
   
  testPanel.revalidate();  
                          
}  
  }  
   
  public static void main(String[] args)  
  {  
    Testy pgui = new Testy();             
  }  
}