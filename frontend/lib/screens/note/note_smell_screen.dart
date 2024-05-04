import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:frontend/constants/colors.dart';
import 'package:frontend/constants/fonts.dart';
import 'package:frontend/widgets/note/note_select_flavour_widget.dart';


import '../../models/wine_model.dart';

import '../../widgets/note/note_wine_card.dart';
import 'note_taste_screen.dart';

class NoteSmellScreen extends StatelessWidget {
  const NoteSmellScreen({super.key});

  @override
  Widget build(BuildContext context) {
    Wine wine = Wine.dummy();

    void navigateNext() {
      Navigator.push(
        context,
        CupertinoPageRoute(
          builder: (context) => NoteTasteScreen(),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text('Color'),
      ),
      body: SingleChildScrollView(
        child: Container(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Container(
                alignment: Alignment.center,
                child: NoteWineCard(wine: wine),
              ),

              SizedBox(height: 20,),
              Text('향기', style: TextStyle(fontSize: AppFontSizes.large),),
              SizedBox(height: 10,),
              Text('어떠한 향기가 나나요?', style: TextStyle(fontSize: AppFontSizes.mediumSmall),),
              SizedBox(height: 30,),
              NoteFlavour(flavourId: [68, 72, 1, 14, 16, 52],),
              SizedBox(height: 20,)
            ],
          ),
        ),
      ),
      persistentFooterButtons: [
        Container(
          height: 40,
          width: double.infinity, // 버튼을 컨테이너 전체 너비로 확장
          decoration: BoxDecoration(
            color: Colors.transparent,
            boxShadow: [], // 그림자 제거
          ),
          child: TextButton(
            onPressed: navigateNext,
            child: Text("다음", style: TextStyle(color: Colors.white)),
            style: TextButton.styleFrom(
              backgroundColor: AppColors.primary, // 버튼 배경 색상
              padding: EdgeInsets.symmetric(horizontal: 30), // 내부 여백
            ),
          ),
        ),
      ],
    );
  }
}
