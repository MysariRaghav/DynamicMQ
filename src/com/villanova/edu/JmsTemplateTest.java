package com.villanova.edu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class JmsTemplateTest implements MessageListener{

	private static String userId;
	private JmsTemplate jmsTemplate;
	private Topic topic;

	@Autowired
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	@Autowired
	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public static void main(String[] args) throws JMSException, IOException {
		// TODO Auto-generated method stub
		 //javaApplication();

		if (args.length != 1)
			System.out.println("User name is required");
		else {
			userId = args[0];

			jmsPublisher();

		}

	}

	private static void jmsPublisher() throws JMSException, IOException {
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext("com/villanova/edu/JmsTemplateTest.xml");

		JmsTemplateTest jmsTemplateTest = (JmsTemplateTest) ctx.getBean("basicJMSChat");

		TopicConnectionFactory connectionFactory = (TopicConnectionFactory) ctx.getBean("mqConnectionFactory");

		TopicConnection tc = connectionFactory.createTopicConnection();

		jmsTemplateTest.publish(tc, jmsTemplateTest.topic, userId);
		jmsTemplateTest.subscribe(tc, jmsTemplateTest.topic, jmsTemplateTest);
	}

	private void subscribe(TopicConnection tc, Topic topic, JmsTemplateTest jmsTemplateTest) throws JMSException {
		// TODO Auto-generated method stub

		TopicSession topicSession= tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicSubscriber topicSubscriber= topicSession.createSubscriber(topic, null, false);
		topicSubscriber.setMessageListener(jmsTemplateTest);
		
	}

	private void publish(TopicConnection tc, Topic topic, String userId) throws JMSException, IOException {
		// TODO Auto-generated method stub

		TopicSession session = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicPublisher topicPublisher = session.createPublisher(topic);
		tc.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			
			String msgToSend=reader.readLine();
			if (msgToSend.equalsIgnoreCase("exit")) {
				tc.close();

				System.exit(0);
			}
			
			else
			{
				TextMessage msg=session.createTextMessage();
				msg.setText(msgToSend);
				topicPublisher.publish(msg);
				
			}
		}

	}

	private static void javaApplication() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("com/villanova/edu/JmsTemplateTest.xml");

		JmsTemplate jmsTemplate = (JmsTemplate) ctx.getBean("jmsTemplate");

		jmsTemplate.send("myqueue", new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {
				// TODO Auto-generated method stub
				TextMessage msg = (TextMessage) session.createTextMessage();
				msg.setText("This is a test");
				return msg;
			}
		});

		TextMessage msg = (TextMessage) jmsTemplate.receive("myqueue");
		try {
			System.out.println(msg.getText());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		
		TextMessage textMessage = (TextMessage) message;
		
		try {
			System.out.println(textMessage.getText());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
