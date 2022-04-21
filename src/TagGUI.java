import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;

public class TagGUI extends JFrame {
    private JPanel mainPanel;

    private JPanel tagsPanel;
    private JTextArea tagsOut;
    private JScrollPane tagsContainer;

    private JPanel ctrlPanel;
    private JButton openFileBtn;
    private JTextField dispFile;
    private JCheckBox scanFile;

    public TagGUI(){
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        createTagsPanel();
        createCtrlPanel();

        mainPanel.add(tagsPanel, BorderLayout.NORTH);
        mainPanel.add(ctrlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void createTagsPanel(){
        tagsPanel = new JPanel();
        tagsOut = new JTextArea(20, 35);
        tagsContainer = new JScrollPane(tagsOut);
        tagsPanel.add(tagsContainer);
    }

    private void createCtrlPanel(){
        ctrlPanel = new JPanel();
        ctrlPanel.setLayout(new BorderLayout());
        dispFile = new JTextField();
        ctrlPanel.add(dispFile, BorderLayout.NORTH);

        JPanel tempPanel = new JPanel();
        openFileBtn = new JButton("Analyze File");
        openFileBtn.addActionListener(e -> analyzeFile());
        scanFile = new JCheckBox("Check for Stop Words");
        scanFile.setSelected(true);
        tempPanel.add(openFileBtn);
        tempPanel.add(scanFile);

        ctrlPanel.add(tempPanel);
    }

    private void analyzeFile(){
        Map<String, Integer> wordMap = new HashMap<String, Integer>();
        ArrayList<String> bookWords = readFile();
        tagsOut.setText("");
        ArrayList<String> stopWords = getStopWords();

        for(String currWord : bookWords){
            if(!stopWords.contains(currWord) || !scanFile.isSelected()){
                if(wordMap.containsKey(currWord))
                    wordMap.put(currWord, wordMap.get(currWord) + 1);
                else
                    wordMap.put(currWord, 1);
            }
        }
        wordMap.remove("");

        wordMap = wordMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        for(Map.Entry<String, Integer> currMap : wordMap.entrySet()) {
            tagsOut.append(currMap.getKey() + "\t" + currMap.getValue() + "\n");
        }

        if(JOptionPane.showConfirmDialog(this, "Would you like to save the extracted tags?") == JOptionPane.YES_OPTION){
            try {
                FileWriter outFile = new FileWriter(dispFile.getText().replaceAll(".txt", "ExtractedTags.txt"));
                outFile.write(tagsOut.getText());
                outFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<String> readFile(){
        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String rec = "";
        ArrayList<String> words = new ArrayList<String>();

        try
        {
            File workingDirectory = new File(System.getProperty("user.dir"));

            chooser.setCurrentDirectory(workingDirectory);
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();
                dispFile.setText(file.toString());
                InputStream in =
                        new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));

                int line = 0;
                while(reader.ready())
                {
                    rec = reader.readLine();
                    line++;
                    for(String word : rec.split(" "))
                        words.add(word.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT));
                }
                reader.close();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return words;
    }

    private ArrayList<String> getStopWords(){
        ArrayList<String> stopWords = new ArrayList<>();
        try {
            File myObj = new File("src/StopWords.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                stopWords.add(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stopWords;
    }
}
