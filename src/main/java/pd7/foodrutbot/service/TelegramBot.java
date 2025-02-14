package pd7.foodrutbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pd7.foodrutbot.config.BotConfig;
import pd7.foodrutbot.entities.MenuItems;
import pd7.foodrutbot.entities.OrderItem;
import pd7.foodrutbot.entities.OrderList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Autowired
    private OrderListService orderListService;

    @Autowired
    private MenuService menuService;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage message = new SendMessage();

            switch (messageText) {
                case "/start":
                    startMenu(chatId);
                    break;
                case "\uD83E\uDD50 Меню":
                    try {
                        openMenu(chatId);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "\uD83D\uDCCB Текущий заказ":
                    currentOrders(chatId);
                    break;
                case "\uD83D\uDC49\uD83C\uDFFB Как пройти":
                    howToPass(chatId);
                    break;
                case "❔ Помощь":
                case "/help":
                    supportInfo(chatId);
                    break;

                default:
                    prepareAndSendMessage(chatId, "Данной команды не существует, откройте меню бота и выберите пункт \"Помощь\" или введите /help");
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                System.out.println("Ошибка при отправке сообщения: " + e.getMessage());
            }
        }
    }

    private void startMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("<b><u>Добро пожаловать в бот кафе «Аквариум»!</u></b>\n\n" +
                "― Чтобы увидеть меню и сделать заказ, нажмите \n «\uD83E\uDD50 Меню».\n\n" +
                "― Узнать статус текущего заказа можно через \n «\uD83D\uDCCB Текущий заказ».\n\n" +
                "― Если вам нужна помощь или есть вопросы, нажмите \n «❔ Помощь».\n\n" +
                "― Чтобы получить информацию о том, как добраться до кафе, используйте \n «\uD83D\uDC49\uD83C\uDFFB Как пройти».\n" +
                "\nДля доступа к функциям используйте клавиатуру ниже.");
        message.enableHtml(true);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("\uD83E\uDD50 Меню");
        keyboardRows.add(firstRow);

        KeyboardRow fouthRow = new KeyboardRow();
        fouthRow.add("\uD83D\uDCCB Текущий заказ");
        keyboardRows.add(fouthRow);

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("\uD83D\uDC49\uD83C\uDFFB Как пройти");
        keyboardRows.add(secondRow);

        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add("❔ Помощь");
        keyboardRows.add(thirdRow);


        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("ошибочка");
        }
    }

    private void openMenu(long chatId) throws TelegramApiException {
        GetChat getChat = new GetChat();
        getChat.setChatId(chatId);
        Chat chat = execute(getChat);
        String username = chat.getFirstName();
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton menuButton = new InlineKeyboardButton();
        menuButton.setText("\uD83C\uDF55 Пора подкрепиться");

        menuButton.setUrl("https://t.me/FoodRUTBot/MenuList");

        keyboard.add(Collections.singletonList(menuButton));
        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(
                "Эй, " + username + "! 😎\n\n" +
                        "Мы знаем, как сложно устоять перед вкусняшками 🍔🍕. Поэтому мы сделали всё, чтобы тебе было удобно и быстро заказывать! " +
                        "Нажми на кнопку ниже и ознакомься с нашими блюдами! 😋"
        );
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /*
     *  Показать список текущих заказов и статус
     */
    private void currentOrders(long chatId) {
        List<OrderList> orders = orderListService.getOrdersByChatId(String.valueOf(chatId));

        String text;
        if (orders != null && !orders.isEmpty()) {
            StringBuilder ordersText = new StringBuilder();

            // Фильтруем заказы с текущим статусом "в очереди"
            orders = orders.stream()
                    .filter(order -> order.getStatus() == OrderList.OrderStatus.ЗАКАЗ_В_ОЧЕРЕДИ)
                    .collect(Collectors.toList());

            if (orders.isEmpty()) {
                text = "У вас нет текущих заказов в очереди.";
            } else {
                for (OrderList order : orders) {
                    ordersText.append("Заказ № ").append(order.getOrderNumber())
                            .append("\nСтатус: ").append(order.getStatus()).append("\n\n");

                    // Перебираем элементы заказа (OrderItem)
                    for (OrderItem orderItem : order.getItems()) {
                        MenuItems menuItem = orderItem.getMenuItem();
                        int quantity = orderItem.getQuantity();

                        if (menuItem != null) {
                            ordersText.append(menuItem.getName()).append(" ")
                                    .append(quantity).append(" шт. - ")
                                    .append(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)))
                                    .append(" руб.\n");
                        } else {
                            ordersText.append("Блюдо с ID ").append(orderItem.getMenuItem().getId())
                                    .append(" не найдено.\n");
                        }
                    }
                    ordersText.append("\n");
                }

                text = ordersText.toString();
            }
        } else {
            text = "У вас нет текущих заказов.";
        }

        // Отправляем сообщение
        prepareAndSendMessage(chatId, text);
    }


    private void howToPass(long chatId) {
        String text = "До кафе «Аквариум» можно попасть с двух КПП:\n\n" +
                "\uD83D\uDEAA *С первого КПП*: Идите вдоль первого корпуса, напротив входа в шестой корпус найдете кафе.\n\n" +
                "\uD83D\uDEAA *Со второго КПП*: Пройдите под аркой третьего корпуса и двигайтесь вдоль шестого — кафе будет совсем рядом.\n\n" +
                "Заходите, будем рады вас видеть!";
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile("https://sun9-27.userapi.com/impg/duj2FVwWPk7MdhY4hC-po5I7D7Ux1LLk_8t2Xw/jPW77ZbOho0.jpg?size=859x685&quality=95&sign=d1a9ea5b0d1e25ab125a663a88f78052&type=album"));
        sendPhoto.setCaption(text);
        sendPhoto.setParseMode("Markdown");
        try{
            execute(sendPhoto);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    private void supportInfo(long chatId) {
        String text = "<b>Как пользоваться ботом:</b>\n\n" +
                "― Нажмите «\uD83E\uDD50 <b>Меню</b>», чтобы увидеть меню нашего кафе. После этого появится кнопка, которая перенесет вас в наше встроенное приложение для удобного заказа.\n\n" +
                "― Чтобы узнать статус вашего текущего заказа, нажмите «\uD83D\uDCCB <b>Текущий заказ</b>». Вы увидите актуальный статус и номер заказа.\n\n" +
                "― Для информации о том, как добраться до кафе, используйте «\uD83D\uDC49\uD83C\uDFFB <b>Как пройти</b>».\n\n" +
                "Все функции доступны через клавиатуру ниже.";
        prepareAndSendMessage(chatId,text);
    }

    public void sendUserNotification(String chatId, String text) {
        prepareAndSendMessage(Long.parseLong(chatId), text);
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }
}
