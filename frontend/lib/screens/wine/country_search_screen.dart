// flutter
import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:frontend/constants/colors.dart';
import 'package:frontend/models/wine_model.dart';
import 'package:frontend/providers/note/note_wine_provider.dart';
import 'package:frontend/screens/note/note_screen.dart';
import 'package:frontend/screens/search/search_camera_screen.dart';
import 'package:frontend/screens/search/search_detail_screen.dart';
import 'package:frontend/services/wine_service.dart';
import 'package:frontend/widgets/feed/feed_wine_item.dart';

// constants
import 'package:frontend/constants/fonts.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';

class CountrySearchScreen extends StatefulWidget {
  String? country;

  CountrySearchScreen({Key? key, this.country = ''}) : super(key: key);

  _CountrySearchScreenState createState() => _CountrySearchScreenState();
}

class _CountrySearchScreenState extends State<CountrySearchScreen> {

  bool isFirstSearch = true;

  late List<CameraDescription> cameras;
  late CameraDescription firstCamera;
  bool _isCameraInitialized = false;

  TextEditingController _searchController = TextEditingController();
  final FocusNode _searchFocusNode = FocusNode();
  List<Wine> _wineList = [];
  bool _isLoading = false;
  Wine? _selectedWine;
  int? _selectedWineId;

  static const _pageSize = 10;
  PagingController<int, Wine> _pagingController =
  PagingController(firstPageKey: 0);

  // _searchWines(String keyword) async {
  //   if (keyword.isEmpty) return;
  //   setState(() {
  //     _isLoading = true;
  //   });
  //   try {
  //     WineService.pageSearchWineList(keyword, 0);
  //     // List<Wine> wineList = await WineService.searchWineList(keyword);
  //     List<Wine> wineList = [];
  //     setState(() {
  //       _wineList = wineList;
  //       _isLoading = false;
  //     });
  //   } catch (e) {
  //     setState(() {
  //       _isLoading = false;
  //     });
  //     print('Failed to fetch wines: $e');
  //   }
  // }

  void _toggleWine(Wine wine) {
    setState(() {
      if (_selectedWineId == wine.id) {
        _selectedWineId = null;
        _selectedWine = null;
      } else {
        _selectedWineId = wine.id;
        _selectedWine = wine;
      }
    });
  }

  Future<void> _fetchCountryPage(int pageKey) async {
    try {
      final newItems =
      await WineService.pageSearchCountryWineList(widget.country!, pageKey);
      final isLastPage = newItems.length < _pageSize;
      if (isLastPage) {
        _pagingController.appendLastPage(newItems);
      } else {
        final nextPageKey = pageKey + 1;
        _pagingController.appendPage(newItems, nextPageKey);
      }
    } catch (error) {
      _pagingController.error = error;
    }
  }

  Future<void> _fetchPage(int pageKey) async {
    try {
      final newItems =
      await WineService.pageSearchWineList(_searchController.text, pageKey);
      final isLastPage = newItems.length < _pageSize;
      if (isLastPage) {
        _pagingController.appendLastPage(newItems);
      } else {
        final nextPageKey = pageKey + 1;
        _pagingController.appendPage(newItems, nextPageKey);
      }
    } catch (error) {
      _pagingController.error = error;
    }
  }

  Future<void> _initCameras() async {
    try {
      cameras = await availableCameras();
      firstCamera = cameras.first;
      setState(() {
        _isCameraInitialized = true;
      });
    } catch (e) {
      print('카메라를 초기화하는 중 에러 발생: $e');
      // 에러 처리 로직 추가
    }
  }

  @override
  void initState() {
    super.initState();
    _initCameras();
    _searchController = TextEditingController(text: widget.country);
    // _pagingController.addPageRequestListener((pageKey) {
    //   _fetchTypePage(pageKey);
    // });
    _pagingController.addPageRequestListener((pageKey) {
      isFirstSearch ? _fetchCountryPage(pageKey) : _fetchPage(pageKey);
    });
    // WidgetsBinding.instance.addPostFrameCallback((_) {
    //   FocusScope.of(context).requestFocus(_searchFocusNode);
    // });
    WidgetsBinding.instance.addPostFrameCallback((_) {
      // 초기 포커스 설정하지만 키보드는 올리지 않음
      _searchFocusNode.canRequestFocus = false;
      FocusScope.of(context).requestFocus(_searchFocusNode);
    });
  }

  @override
  void dispose() {
    _pagingController.dispose();
    super.dispose();
  }

  void setPagingFunction() {
    _pagingController.itemList = [];
    _pagingController.removePageRequestListener((pageKey){_fetchCountryPage(pageKey);});
    _pagingController.addPageRequestListener((pageKey) {
      _fetchPage(pageKey);
    });
  }

  void _resetAndChangePagingListener() {
    _pagingController.itemList = []; // 현재 페이지 데이터 초기화
    _pagingController.nextPageKey = 0; // 페이지 키 초기화
    _pagingController.removePageRequestListener((pageKey) {
      isFirstSearch ? _fetchCountryPage(pageKey) : _fetchPage(pageKey);
    });
    isFirstSearch = false; // 첫 검색이 아니므로 플래그 변경
    _pagingController.addPageRequestListener(_fetchPage); // 새 리스너 설정
    _pagingController.refresh(); // 페이지 컨트롤러 새로고침
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: GestureDetector(
        onTap: () {
          FocusScope.of(context).unfocus();
        },
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: TextField(
                focusNode: _searchFocusNode,
                onSubmitted: (value) {
                  FocusScope.of(context).unfocus();
                  if (value == '') {
                    ScaffoldMessenger.of(context)
                        .showSnackBar(SnackBar(content: Text('검색어를 입력해주세요!')));
                  } else {
                    // _searchWines(value);
                    // setPagingFunction();
                    // _pagingController.refresh();
                    _searchController.text = value;
                    _resetAndChangePagingListener();
                  }
                },
                controller: _searchController,
                decoration: InputDecoration(
                  suffix: Row(
                    mainAxisSize: MainAxisSize.min, // Row가 차지하는 공간을 최소로 하여 아이콘들이 압축되지 않게 함
                    children: <Widget>[
                      IconButton(
                        icon: Icon(Icons.camera_alt),
                        onPressed: _isCameraInitialized
                            ? () {
                          Navigator.of(context).pushReplacement(
                            MaterialPageRoute(
                              builder: (context) =>
                                  SearchCameraScreen(camera: firstCamera),
                            ),
                          );
                        }
                        : null
                      ),
                      IconButton(
                        icon: Icon(Icons.clear), // 두 번째 아이콘 예시로 'clear' 아이콘을 추가
                        onPressed: () {
                          setState(() {
                            _searchController.clear();
                            FocusScope.of(context).requestFocus(_searchFocusNode);
                          });// 텍스트 필드 내용 지우기
                        },
                      ),
                    ],
                  ),
                  prefixIcon: IconButton(
                    icon: Icon(Icons.arrow_back),
                    onPressed: () {
                      Navigator.of(context).pop();
                    },
                  ),
                  focusedBorder: UnderlineInputBorder(
                    borderSide: BorderSide(
                      color: AppColors.primary,
                    ),
                  ),
                ),
              ),
            ),
            Expanded(
              child: _isLoading
                  ? Center(child: CircularProgressIndicator())
                  : PagedListView<int, Wine>(
                pagingController: _pagingController,
                builderDelegate: PagedChildBuilderDelegate<Wine>(
                  itemBuilder: (listContext, item, index) =>
                      GestureDetector(
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => SearchDetailScreen(
                                wineId: item.id!,
                              ),
                            ),
                          );
                        },
                        child: Container(
                          child: FeedWineItem(
                            wine: item,
                            isSelected: item.id == _selectedWineId,
                          ),
                        ),
                      ),
                  noItemsFoundIndicatorBuilder: (listContext) =>
                  //TODO: 검색을 하지 않아서 현재 검색 결과가 비어 있는 경우
                  Center(
                    child: Text(
                      '🔍\n검색된 와인이 없습니다!\n다른 키워드로 검색해볼까요?\n✏',
                      style: TextStyle(
                        fontSize: AppFontSizes.mediumLarge,
                        fontWeight: FontWeight.bold,
                      ),
                      textAlign: TextAlign.center,
                    ),
                  ),
                  firstPageErrorIndicatorBuilder: (listContext) =>
                  //TODO: 검색어가 괴상해서 검색 결과가 안 나오는 경우
                  Text(
                    '\n검색 결과가 없습니다.\n다른 검색어로 새로운 와인을 찾아보세요!',
                    style: TextStyle(
                      fontSize: AppFontSizes.mediumSmall,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  newPageErrorIndicatorBuilder: (listContext) =>
                  //TODO: 검색 결과가 있지만 끝을 봐서 아래에 알려줄 문구
                  Text(
                    '\n🔍 더 이상 표시할 와인이 없습니다!\n다른 검색어로 새로운 와인을 찾아보세요! 🧭',
                    style: TextStyle(
                      fontSize: AppFontSizes.mediumSmall,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}