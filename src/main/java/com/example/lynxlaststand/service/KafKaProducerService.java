package com.example.lynxlaststand.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.example.lynxlaststand.model.Client;

@Service
public class KafKaProducerService {

	private static final Logger logger = LoggerFactory.getLogger(KafKaProducerService.class);

	private String topicName = "client-log";

	@Autowired
	private KafkaTemplate<String, Client> clientKafkaTemplate;

	public void saveCreateClientLog() {

		String jdbcUrl = "jdbc:sqlite:/C:\\eclipse-workspace\\lynx-last-stand-producer\\marketing_campaign.db";
		try {
			Connection connection = DriverManager.getConnection(jdbcUrl);
			String sql = "SELECT * FROM marketing_campaign";
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);

			Client client = new Client();

			while (result.next()) {

				// assign values from database to newly created client object
				client.setId(Integer.valueOf(result.getString("ID")));

				if (result.getString("income").equals("")) {
					client.setIncome(0);
				} else {
					client.setIncome(Double.valueOf(result.getString("income")));
				}

				client.setMaritalStatus(result.getString("Marital_Status"));
				client.setMntFishProducts(Double.valueOf(result.getString("MntFishProducts")));
				client.setMntFruits(Double.valueOf(result.getString("MntFruits")));
				client.setMntGoldProducts(Double.valueOf(result.getString("MntGoldProds")));
				client.setMntMeatProducts(Double.valueOf(result.getString("MntMeatProducts")));
				client.setMntSweetProducts(Double.valueOf(result.getString("MntSweetProducts")));
				client.setMntWines(Double.valueOf(result.getString("MntWines")));

				ListenableFuture<SendResult<String, Client>> future = this.clientKafkaTemplate.send(topicName, client);

				future.addCallback(new ListenableFutureCallback<SendResult<String, Client>>() {
					@Override
					public void onSuccess(SendResult<String, Client> result) {
						logger.info(
								"Client created: " + client + " with offset: " + result.getRecordMetadata().offset());
					}

					@Override
					public void onFailure(Throwable ex) {
						logger.error("Client created : " + client, ex);

					}

				});

			}

		} catch (SQLException e) {
			System.out.println("Error connecting to SQLite database");
			e.printStackTrace();
		}
	}

}
