import 'package:flutter/material.dart';
import 'package:frontend/models/feed.dart';
import 'package:frontend/widgets/common/templates/feed_item_widget.dart';

class FeedDetailScreen extends StatelessWidget {
  Feed feed;

  FeedDetailScreen(this.feed);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Feed Detail')),
      body: FeedItem(
        feed: feed,
      ),
    );
  }
}
