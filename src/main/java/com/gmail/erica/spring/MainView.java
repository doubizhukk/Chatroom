package com.gmail.erica.spring;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.awt.*;

@Route("")
@Push
@StyleSheet("frontend://styles/styles.css")
public class MainView extends VerticalLayout {

    private String username;
    private UnicastProcessor<ChatMessage> publisher;
    private Flux<ChatMessage> messages;
    private Text text;

    public MainView(UnicastProcessor<ChatMessage> publisher, Flux<ChatMessage> messages) {
        this.publisher = publisher;
        this.messages = messages;
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setSizeFull();
        addClassName("main-view");

        H1 header = new H1("Wechat");
        header.getElement().getThemeList().add("dark");
        text = new Text("Welcome to the chat room. Please enter your name and start chatting");

        add(header);
        add(text);
        askUsername();
    }

    private void askUsername() {
        HorizontalLayout usernameLayout = new HorizontalLayout();
        TextField usernameField = new TextField();
        Button startButton = new Button("Start Chatting");
        usernameLayout.add(usernameField, startButton);
        
        startButton.addClickListener(click -> {
            username = usernameField.getValue();
            remove(usernameLayout);
            remove(text);
            showChat();
        });
        add(usernameLayout);
    }

    private void showChat() {
        MessageList messageList = new MessageList();

        add(messageList, createInputLayout());

        expand(messageList);

        messages.subscribe(message -> {
            getUI().ifPresent(ui ->
                    ui.access(() ->
                        messageList.add(new Paragraph(
                    message.getFrom() + " @ " +
                            message.getTime().getHour() + ":" +
                            message.getTime().getMinute() + ":" +
                            message.getTime().getSecond() + " " +
                            message.getTime().getYear() + "-" +
                            message.getTime().getMonthValue() + "-" +
                            message.getTime().getDayOfMonth() + " said: " +
                            message.getMessage()
            ))

            ));

        });
    }

    private Component createInputLayout() {
        HorizontalLayout inputLayout = new HorizontalLayout();
        inputLayout.setWidth("100%");

        TextField messageField = new TextField();
        Button sendButton = new Button("Send");
        sendButton.getElement().getThemeList().add("primary");

        inputLayout.add(messageField, sendButton);
        inputLayout.expand(messageField);

        sendButton.addClickListener(click -> {
            publisher.onNext(new ChatMessage(username, messageField.getValue()));
            messageField.clear();
            messageField.focus();
        });

        messageField.focus();
        return inputLayout;
    }

}
