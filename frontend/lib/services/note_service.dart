import 'package:frontend/models/ai_chat_model.dart';
import 'package:frontend/providers/note/note_wine_provider.dart';
import 'package:frontend/utils/api_client.dart';

import '../models/note_model.dart';

class AiChatService {
  static Future<dynamic> postSurvey(AiChat aiChat) async {
    final response = await ApiClient().dio.post('/ai-chat', data: aiChat);
    if (response.statusCode == 200) {
      return response.statusCode;
    }
    throw Error();
  }
}

class NoteListService {
  static Future<List<WineNoteCard>> getNoteList() async {
    try {
      final response = await ApiClient().dio.get('/tasting-notes');
      if (response.statusCode == 200) {
        List<dynamic> noteJsonList = response.data as List;
        List<WineNoteCard> notes =
            noteJsonList.map((json) => WineNoteCard.fromJson(json)).toList();
        return notes;
      } else {
        throw Exception("Failed to load wine notes");
      }
    } catch (e) {
      throw Exception("An error occurred while fetching data: $e");
    }
  }
}

class NoteDetailService {
  static Future<NoteDetailModel> getNoteDetail(int id) async {
    try {
      final response = await ApiClient().dio.get('/tasting-notes/$id');
      if (response.statusCode == 200) {
        // 응답 데이터를 NoteDetail 객체로 변환
        return NoteDetailModel.fromJson(response.data);
      } else {
        throw Exception("Failed to load wine notes");
      }
    } catch (e) {
      throw Exception("An error occurred while fetching data: $e");
    }
  }
}

class NoteCreateService {
  static Future<void> createNote(NoteProvider noteProvider) async {
    try {
      var jsonData = noteProvider.toJson(); // 가정: toJson() 메서드 구현
      final response = await ApiClient().dio.post('/tasting-notes', data: jsonData);
      // 상태 코드가 200대인 경우 성공으로 처리
      if (response.statusCode != null && response.statusCode! >= 200 && response.statusCode! < 300) {
        print("Note successfully created.");
      } else {
        throw Exception("Failed to create note: ${response.statusCode}");
      }
    } catch (e) {
      throw Exception("An error occurred while creating the note: $e");
    }
  }
}