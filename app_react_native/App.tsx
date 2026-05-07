import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import type { BottomTabScreenProps } from '@react-navigation/bottom-tabs';
import * as Speech from 'expo-speech';
import { StatusBar } from 'expo-status-bar';
import { useEffect, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import {
  ActivityIndicator,
  Animated,
  Easing,
  Image,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import type { DimensionValue, ImageSourcePropType, ViewStyle } from 'react-native';

import { readJson, readText, writeJson, writeText } from './src/storage';

type CompanionId = 'fox' | 'deer' | 'bear';
type Mood = '平静' | '低落' | '焦虑' | '疲惫' | '被接住';
type TabParamList = {
  Heal: undefined;
  Feed: undefined;
  Diary: undefined;
  Memory: undefined;
};
type TabProps<T extends keyof TabParamList> = BottomTabScreenProps<TabParamList, T>;

interface Companion {
  id: CompanionId;
  name: string;
  mark: string;
  sticker: string;
  title: string;
  phrase: string;
  scene: string;
  color: string;
  soft: string;
}

interface NoteEntry {
  id: string;
  mood: Mood;
  text: string;
  createdAt: string;
}

interface FeedPost {
  id: string;
  authorId: CompanionId;
  title: string;
  time: string;
  body: string;
  imageLabel?: string;
  comments: string[];
  reactions: number;
  userOwned?: boolean;
}

interface TroubleOption {
  id: string;
  title: string;
  detail: string;
  result: string;
}

interface RoomMessage {
  id: string;
  role: 'user' | 'companion';
  companionId: CompanionId;
  text: string;
  createdAt: string;
}

const STORAGE_KEYS = {
  companion: 'forest_room_companion_v2',
  notes: 'forest_room_notes_v2',
  trouble: 'forest_room_trouble_v2',
  feedPosts: 'forest_room_feed_posts_v2',
  roomMessages: 'forest_room_messages_v2',
};

const companions: Companion[] = [
  {
    id: 'fox',
    name: '花花狸',
    mark: '狸',
    sticker: '海边吹风',
    title: '叛逆调皮的吐槽搭子',
    phrase: '带你可以，但你不准在后座大喊大叫破坏我的帅气形象。',
    scene: '适合想吐槽、想被逗一下、脑子停不下来的时候。',
    color: '#d96f3f',
    soft: '#fff0dd',
  },
  {
    id: 'deer',
    name: '森森鹿',
    mark: '鹿',
    sticker: '元气复活',
    title: '温柔理性的心事翻译官',
    phrase: '我会慢慢听你讲，不急着评判，也不急着把你变好。',
    scene: '适合委屈、低落、想被安静接住的时候。',
    color: '#55a58c',
    soft: '#e9f7ef',
  },
  {
    id: 'bear',
    name: '咕咕熊',
    mark: '熊',
    sticker: '疯狂感动',
    title: '憨厚可靠的能量补给站',
    phrase: '我可以带零食去边吃边看吗？看别人运动，等于我也运动。',
    scene: '适合疲惫、空掉、想从小动作恢复一点能量的时候。',
    color: '#b98652',
    soft: '#fff3d7',
  },
];

const moodOptions: Mood[] = ['平静', '低落', '焦虑', '疲惫', '被接住'];

const feedPosts: FeedPost[] = [
  {
    id: 'feed-screening-party',
    authorId: 'deer',
    title: '那场清空收藏夹的荒诞放映会',
    time: '37分钟前',
    body: '在咖啡馆跟花花狸大声抱怨这种“知识仓鼠症”带来的内耗，结果这家伙居然建议把落灰的视频放出来当喜剧看。',
    comments: ['咕咕熊：那我可以带零食去边吃边看吗？', '花花狸：多带点薯片，看别人受苦没点碳水怎么行。'],
    reactions: 16,
  },
  {
    id: 'feed-river-walk',
    authorId: 'deer',
    title: '原来大家都在假装自律啊',
    time: '3小时前',
    body: '在河边散步碰到咕咕熊，聊了一下才震惊地发现，原来这只憨憨熊也被“收藏夹里的完美自己”折磨着。',
    imageLabel: '河边自拍',
    comments: ['森森鹿：心里的沉重感好像真的减轻了一大半。'],
    reactions: 23,
  },
  {
    id: 'feed-sea-wind',
    authorId: 'fox',
    title: '闪亮',
    time: '5小时前',
    body: '海边风景好棒！吹吹风心情是不是好多了？下次带我一起去兜风嘛！',
    comments: ['森森鹿：哇！海边风景好好！', '花花狸：回复森森鹿：先别破坏我的咖气形象。'],
    reactions: 31,
  },
];

const troubleOptions: TroubleOption[] = [
  {
    id: 'tired',
    title: '很疲惫但提不起劲',
    detail: '不是身体上的疲惫，而是一种莫名的滞重感阻挡了自己。',
    result: '困倦疲劳长期围困，生活被无力感透支，需要重新激活能量。',
  },
  {
    id: 'tense',
    title: '脑子乱静不下来',
    detail: '经常处于一种紧绷状态，感觉被无形的消耗拖住，很难放松。',
    result: '你的注意力正在被过多念头占用，可以先把它们倒进树洞里。',
  },
  {
    id: 'lost',
    title: '心里空感到迷茫',
    detail: '感觉被日子推着走，虽然每天都在忙，但找不到明确意义感。',
    result: '你需要的不是立刻找到答案，而是先找回一点和自己的连接。',
  },
];

const weeklyMood = [42, 48, 45, 58, 62, 70, 79];
const starPoints: Array<{ left: DimensionValue; top: DimensionValue }> = [
  { left: '12%', top: '20%' },
  { left: '22%', top: '48%' },
  { left: '33%', top: '30%' },
  { left: '48%', top: '18%' },
  { left: '56%', top: '55%' },
  { left: '71%', top: '34%' },
  { left: '84%', top: '22%' },
  { left: '88%', top: '60%' },
];

const defaultNotes: NoteEntry[] = [
  {
    id: 'seed-note',
    mood: '被接住',
    text: '今天我愿意先把心事放到林间，不急着解决，只先让它被看见。',
    createdAt: new Date().toISOString(),
  },
];

const defaultRoomMessages: RoomMessage[] = [
  {
    id: 'room-welcome',
    role: 'companion',
    companionId: 'deer',
    text: '欢迎来到趣聊房间。你可以打字告诉我：今天最消耗你的是什么？',
    createdAt: new Date().toISOString(),
  },
];

const Tab = createBottomTabNavigator<TabParamList>();

const spriteFrames: Record<CompanionId, ImageSourcePropType[]> = {
  fox: [
    require('./assets/sprites/fox/idle_0.png'),
    require('./assets/sprites/fox/idle_1.png'),
    require('./assets/sprites/fox/idle_2.png'),
    require('./assets/sprites/fox/idle_3.png'),
    require('./assets/sprites/fox/idle_4.png'),
    require('./assets/sprites/fox/idle_5.png'),
    require('./assets/sprites/fox/idle_6.png'),
    require('./assets/sprites/fox/idle_7.png'),
  ],
  deer: [
    require('./assets/sprites/deer/idle_0.png'),
    require('./assets/sprites/deer/idle_1.png'),
    require('./assets/sprites/deer/idle_2.png'),
    require('./assets/sprites/deer/idle_3.png'),
    require('./assets/sprites/deer/idle_4.png'),
    require('./assets/sprites/deer/idle_5.png'),
    require('./assets/sprites/deer/idle_6.png'),
    require('./assets/sprites/deer/idle_7.png'),
  ],
  bear: [
    require('./assets/sprites/bear/idle_0.png'),
    require('./assets/sprites/bear/idle_1.png'),
    require('./assets/sprites/bear/idle_2.png'),
    require('./assets/sprites/bear/idle_3.png'),
    require('./assets/sprites/bear/idle_4.png'),
    require('./assets/sprites/bear/idle_5.png'),
    require('./assets/sprites/bear/idle_6.png'),
    require('./assets/sprites/bear/idle_7.png'),
  ],
};

export default function App() {
  const [hydrated, setHydrated] = useState(false);
  const [companionId, setCompanionId] = useState<CompanionId>('deer');
  const [notes, setNotes] = useState<NoteEntry[]>(defaultNotes);
  const [mood, setMood] = useState<Mood>('被接住');
  const [noteDraft, setNoteDraft] = useState('');
  const [roomDraft, setRoomDraft] = useState('');
  const [feedDraft, setFeedDraft] = useState('');
  const [customFeedPosts, setCustomFeedPosts] = useState<FeedPost[]>([]);
  const [roomMessages, setRoomMessages] = useState<RoomMessage[]>(defaultRoomMessages);
  const [selectedTroubleId, setSelectedTroubleId] = useState('tired');
  const [speaking, setSpeaking] = useState(false);
  const [roomOpen, setRoomOpen] = useState(false);
  const [detailCompanionId, setDetailCompanionId] = useState<CompanionId | null>(null);
  const [growthReportOpen, setGrowthReportOpen] = useState(false);

  const companion = companions.find((item) => item.id === companionId) ?? companions[1];
  const detailCompanion = companions.find((item) => item.id === detailCompanionId) ?? companion;
  const selectedTrouble = troubleOptions.find((item) => item.id === selectedTroubleId) ?? troubleOptions[0];
  const latestNote = notes[0] ?? defaultNotes[0];
  const visibleFeedPosts = [...customFeedPosts, ...feedPosts];

  useEffect(() => {
    let mounted = true;

    async function hydrate() {
      const [storedCompanion, storedNotes, storedTrouble, storedFeedPosts, storedRoomMessages] = await Promise.all([
        readText(STORAGE_KEYS.companion, 'deer'),
        readJson<NoteEntry[]>(STORAGE_KEYS.notes, defaultNotes),
        readText(STORAGE_KEYS.trouble, 'tired'),
        readJson<FeedPost[]>(STORAGE_KEYS.feedPosts, []),
        readJson<RoomMessage[]>(STORAGE_KEYS.roomMessages, defaultRoomMessages),
      ]);

      if (!mounted) return;
      if (storedCompanion === 'fox' || storedCompanion === 'deer' || storedCompanion === 'bear') {
        setCompanionId(storedCompanion);
      }
      if (storedNotes.length) setNotes(storedNotes);
      if (troubleOptions.some((item) => item.id === storedTrouble)) setSelectedTroubleId(storedTrouble);
      setCustomFeedPosts(storedFeedPosts);
      setRoomMessages(storedRoomMessages.length ? storedRoomMessages : defaultRoomMessages);
      setHydrated(true);
    }

    void hydrate();

    return () => {
      mounted = false;
      Speech.stop();
    };
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    void writeText(STORAGE_KEYS.companion, companionId);
  }, [companionId, hydrated]);

  useEffect(() => {
    if (!hydrated) return;
    void writeJson(STORAGE_KEYS.notes, notes);
  }, [notes, hydrated]);

  useEffect(() => {
    if (!hydrated) return;
    void writeText(STORAGE_KEYS.trouble, selectedTroubleId);
  }, [selectedTroubleId, hydrated]);

  useEffect(() => {
    if (!hydrated) return;
    void writeJson(STORAGE_KEYS.feedPosts, customFeedPosts);
  }, [customFeedPosts, hydrated]);

  useEffect(() => {
    if (!hydrated) return;
    void writeJson(STORAGE_KEYS.roomMessages, roomMessages);
  }, [roomMessages, hydrated]);

  function saveNote(source?: string) {
    const text = (source ?? noteDraft ?? roomDraft).trim();
    if (!text) return;

    setNotes((prev) => [
      {
        id: `note-${Date.now()}`,
        mood,
        text,
        createdAt: new Date().toISOString(),
      },
      ...prev,
    ]);
    setNoteDraft('');
    setRoomDraft('');
  }

  function createFeedPost(seed?: string) {
    const body = (seed ?? feedDraft).trim();
    if (!body) return;

    const post: FeedPost = {
      id: `feed-${Date.now()}`,
      authorId: companionId,
      title: buildPostTitle(body),
      time: '刚刚',
      body,
      imageLabel: mood === '被接住' ? '林间自拍' : undefined,
      comments: [
        `${companion.name}：收到你的碎念了，我先把它放进收藏夹，不让它一个人乱跑。`,
        companionId === 'bear' ? '咕咕熊：这个可以配热牛奶。' : '咕咕熊：我可以带零食围观吗？',
      ],
      reactions: 1,
      userOwned: true,
    };

    setCustomFeedPosts((prev) => [post, ...prev]);
    setFeedDraft('');
  }

  function sendRoomMessage(seed?: string) {
    const text = (seed ?? roomDraft).trim();
    if (!text) return;

    const now = new Date().toISOString();
    const userMessage: RoomMessage = {
      id: `room-user-${Date.now()}`,
      role: 'user',
      companionId,
      text,
      createdAt: now,
    };
    const reply: RoomMessage = {
      id: `room-reply-${Date.now()}`,
      role: 'companion',
      companionId,
      text: buildRoomReply(text, companion, selectedTrouble),
      createdAt: now,
    };

    setRoomMessages((prev) => [...prev, userMessage, reply]);
    setRoomDraft('');
  }

  function saveTroubleToDiary() {
    saveNote(`我现在最明显的困扰是：${selectedTrouble.title}。${selectedTrouble.result}`);
  }

  function speak(text: string) {
    if (speaking) {
      Speech.stop();
      setSpeaking(false);
      return;
    }

    setSpeaking(true);
    Speech.speak(text, {
      language: 'zh-CN',
      pitch: 1,
      rate: 0.88,
      onDone: () => setSpeaking(false),
      onStopped: () => setSpeaking(false),
      onError: () => setSpeaking(false),
    });
  }

  function renderHeal() {
    return (
      <ScreenFrame tone="meadow">
        <View style={styles.homeTop}>
          <View>
            <View style={styles.timerPill}>
              <Text style={styles.timerIcon}>◷</Text>
              <Text style={styles.timerText}>10:00</Text>
              <Text style={styles.timerPlus}>＋</Text>
            </View>
            <Text style={styles.restoreText}>每天00:00将恢复</Text>
          </View>
          <View style={styles.avatarBubble}>
            <Text style={styles.avatarText}>你</Text>
          </View>
        </View>

        <Text style={styles.homeGreeting}>下午好，漫长的午后一起来聊聊吧</Text>

        <View style={styles.meadowStage}>
          <View style={styles.cloudOne} />
          <View style={styles.cloudTwo} />
          <MeadowDecor />
          <AnimalScene companion={companions[0]} style={styles.foxSpot} onPress={() => setDetailCompanionId('fox')} />
          <AnimalScene companion={companions[2]} style={styles.bearSpot} onPress={() => setDetailCompanionId('bear')} />
          <AnimalScene companion={companions[1]} style={styles.deerSpot} onPress={() => setDetailCompanionId('deer')} />
          <View style={styles.picnicMat}>
            <Text style={styles.picnicText}>☕</Text>
          </View>
          <View style={styles.snowGlobe}>
            <Text style={styles.snowGlobeText}>小森林</Text>
          </View>
          <HealButton onPress={() => setRoomOpen(true)} />
        </View>

        <Text style={styles.tapHint}>点小动物查看档案，点「聊愈」进入房间</Text>
      </ScreenFrame>
    );
  }

  function renderFeed() {
    return (
      <ScreenFrame tone="meadow">
        <View style={styles.feedHeader}>
          <Text style={styles.bubbleMenu}>···</Text>
          <View style={styles.avatarBubbleSmall}>
            <Text style={styles.avatarTextSmall}>你</Text>
          </View>
        </View>

        <View style={styles.storyBoard}>
          <View style={styles.stickerRow}>
            {companions.map((item) => (
              <Pressable key={item.id} onPress={() => setCompanionId(item.id)} style={styles.stickerCard}>
                <IdleAnimal companion={item} />
                <Text style={styles.stickerText}>{feedStickerTitle(item.id)}</Text>
              </Pressable>
            ))}
          </View>
          <View style={styles.eventCard}>
            <View style={styles.fakePhoto}>
              <Text style={styles.fakePhotoText}>雨天客厅</Text>
              <Text style={styles.fakePhotoSub}>花花狸 森森鹿 咕咕熊</Text>
            </View>
            <View style={styles.flexFill}>
              <Text style={styles.eventTitle}>那场兔去舟车劳顿的客厅露营</Text>
              <Text style={styles.eventDate}>4月30日 周四</Text>
            </View>
          </View>
        </View>

        <View style={styles.broadcast}>
          <Text style={styles.broadcastIcon}>喇叭</Text>
          <Text style={styles.broadcastText}>14:45 阴 受空气湿度影响，小森林内部的徒步计划改成室内露营。</Text>
        </View>

        <View style={styles.feedComposer}>
          <Text style={styles.feedComposerTitle}>写一条碎念</Text>
          <TextInput
            value={feedDraft}
            onChangeText={setFeedDraft}
            style={styles.feedInput}
            placeholder="例如：今天收藏夹又满了，但我还是没开始学。"
            placeholderTextColor="#91a59e"
            multiline
          />
          <View style={styles.actionRow}>
            <GhostButton label="套用伙伴吐槽" onPress={() => setFeedDraft(companion.phrase)} />
            <PrimaryButton label="发布" onPress={() => createFeedPost()} />
          </View>
        </View>

        {visibleFeedPosts.map((post) => {
          const author = companions.find((item) => item.id === post.authorId) ?? companions[1];
          return <PostCard key={post.id} post={post} author={author} />;
        })}

        <Pressable style={styles.floatPlus} onPress={() => setFeedDraft(companion.phrase)}>
          <Text style={styles.floatPlusText}>＋</Text>
        </Pressable>
      </ScreenFrame>
    );
  }

  function renderDiary() {
    return (
      <ScreenFrame tone="paper">
        <View style={styles.segment}>
          <Text style={styles.segmentMuted}>重启人生</Text>
          <Text style={styles.segmentActive}>日记</Text>
        </View>

        <View style={styles.weekCard}>
          <View style={styles.cardHeaderRow}>
            <Text style={styles.paperTitle}>本周情绪变化</Text>
            <Text style={styles.calendarChip}>月历</Text>
          </View>
          <View style={styles.weekRow}>
            {['26\n周日', '27\n周一', '28\n周二', '29\n周三', '30\n周四', '1\n周五', '2\n周六'].map((item, index) => (
              <View key={item} style={styles.weekDay}>
                <View style={[styles.weekDot, index === 3 && styles.weekDotActive]} />
                <Text style={[styles.weekText, index === 3 && styles.weekTextActive]}>{item}</Text>
              </View>
            ))}
          </View>
        </View>

        <View style={styles.chartCard}>
          <View style={styles.cardHeaderRow}>
            <View>
              <Text style={styles.chartTitle}>每一步，都值得被记录</Text>
              <Text style={styles.chartSub}>在今天的轨迹 79%</Text>
            </View>
            <Text style={styles.calendarChip}>月历</Text>
          </View>
          <MoodChart values={weeklyMood} />
          <Text style={styles.goalTitle}>阶段目标：找到好工作</Text>
          <Text style={styles.notePreview}>{latestNote.text}</Text>
          <PrimaryButton label="去聊愈，开启你的心灵成长轨迹" onPress={() => setRoomOpen(true)} />
        </View>

        <View style={styles.doorCard}>
          <Text style={styles.doorQuestion}>这段时间，最让你心累的困扰是？</Text>
          <View style={styles.magicDoor}>
            <Text style={styles.doorLight}>门</Text>
          </View>
          {troubleOptions.map((option) => (
            <Pressable
              key={option.id}
              onPress={() => setSelectedTroubleId(option.id)}
              style={[styles.troubleOption, selectedTroubleId === option.id && styles.troubleOptionActive]}
            >
              <Text style={styles.troubleTitle}>{option.title}</Text>
              <Text style={styles.troubleDetail}>{option.detail}</Text>
            </Pressable>
          ))}
          <View style={styles.resultTags}>
            <Text style={styles.resultTag}>{selectedTrouble.result}</Text>
          </View>
          <View style={styles.doorActions}>
            <GhostButton label="换一个困扰" onPress={() => setSelectedTroubleId(nextTroubleId(selectedTroubleId))} />
            <PrimaryButton label="写进日记" onPress={saveTroubleToDiary} />
          </View>
        </View>
      </ScreenFrame>
    );
  }

  function renderMemory() {
    return (
      <ScreenFrame tone="night">
        <View style={styles.starMap}>
          <View style={styles.nebulaOne} />
          <View style={styles.nebulaTwo} />
          {starPoints.map((point, index) => (
            <TwinkleStar key={`${point.left}-${point.top}`} point={point} index={index} />
          ))}
          <View style={styles.orbitOuter} />
          <View style={styles.orbitInner} />
          <View style={styles.userInStars}>
            <Text style={styles.userInStarsText}>你</Text>
          </View>
          <View style={styles.sideActions}>
            <IconButton title="所有星图" mark="星" />
            <IconButton title="人格解读" mark="问" />
          </View>
        </View>

        <View style={styles.noteComposer}>
          <Text style={styles.paperTitle}>把这次聊愈留在记忆里</Text>
          <View style={styles.moodRow}>
            {moodOptions.map((item) => {
              const active = item === mood;
              return (
                <Pressable key={item} onPress={() => setMood(item)} style={[styles.moodChip, active && styles.moodChipActive]}>
                  <Text style={[styles.moodText, active && styles.moodTextActive]}>{item}</Text>
                </Pressable>
              );
            })}
          </View>
          <TextInput
            value={noteDraft}
            onChangeText={setNoteDraft}
            style={styles.noteInput}
            placeholder="例如：今天虽然很累，但我愿意先陪自己十分钟。"
            placeholderTextColor="#91a59e"
            multiline
          />
          <View style={styles.actionRow}>
            <GhostButton label={speaking ? '停止朗读' : '朗读问候'} onPress={() => speak(buildPostcard(latestNote, companion))} />
            <PrimaryButton label="保存日记" onPress={() => saveNote(noteDraft || latestRoomUserText(roomMessages))} />
          </View>
        </View>

        {notes.map((entry) => (
          <View key={entry.id} style={styles.memoryNote}>
            <Text style={styles.memoryMeta}>{entry.mood} · {formatDate(entry.createdAt)}</Text>
            <Text style={styles.memoryText}>{entry.text}</Text>
          </View>
        ))}
      </ScreenFrame>
    );
  }

  function renderChatRoom() {
    return (
      <SafeAreaView style={styles.chatRoomSafe}>
        <StatusBar style="light" />
        <View style={styles.chatRoomScene}>
          <RoomDecor />
          <View style={styles.chatRoomTop}>
            <Pressable style={styles.chatRoomBack} onPress={() => setRoomOpen(false)}>
              <Text style={styles.chatRoomBackText}>‹</Text>
            </Pressable>
            <Text style={styles.chatRoomTimer}>10:00</Text>
            <Pressable style={styles.chatRoomSound} onPress={() => speak(companion.phrase)}>
              <Text style={styles.chatRoomSoundText}>声</Text>
            </Pressable>
          </View>

          <View style={styles.chatRoomCenter}>
            <Text style={styles.roomWindow}>窗外下雪</Text>
            <IdleAnimal companion={companion} large />
            <Text style={styles.roomName}>{companion.name}</Text>
            <Pressable onPress={() => setGrowthReportOpen(true)}>
              <VoiceBars />
            </Pressable>
            <Text style={styles.aiHint}>回复由AI生成</Text>
          </View>

          <View style={styles.roomMessageStack}>
            {roomMessages.slice(-4).map((message) => {
              const author = companions.find((item) => item.id === message.companionId) ?? companion;
              return (
                <View
                  key={message.id}
                  style={[
                    styles.roomBubble,
                    message.role === 'user' ? styles.roomBubbleUser : styles.roomBubbleCompanion,
                  ]}
                >
                  <Text style={styles.roomBubbleAuthor}>{message.role === 'user' ? '你' : author.name}</Text>
                  <Text style={styles.roomBubbleText}>{message.text}</Text>
                </View>
              );
            })}
          </View>

          <View style={styles.chatRoomShortcuts}>
            <Pressable style={styles.roomShortcut} onPress={() => setRoomDraft('今天我想先打字说说')}>
              <Text style={styles.roomShortcutIcon}>⌨</Text>
              <Text style={styles.roomShortcutText}>打字</Text>
            </Pressable>
            <Pressable style={styles.roomShortcut} onPress={() => sendRoomMessage(companion.phrase)}>
              <Text style={styles.roomShortcutIcon}>●●</Text>
              <Text style={styles.roomShortcutText}>趣聊</Text>
            </Pressable>
          </View>

          <View style={styles.chatRoomInputDock}>
            <Text style={styles.roomPlus}>＋</Text>
            <TextInput
              value={roomDraft}
              onChangeText={setRoomDraft}
              style={styles.roomInput}
              placeholder="说得越详细，回应效果越好"
              placeholderTextColor="#91a59e"
            />
            <Pressable style={styles.roomSend} onPress={() => sendRoomMessage()}>
              <Text style={styles.roomSendText}>发送</Text>
            </Pressable>
          </View>

          <Pressable style={styles.growthEntry} onPress={() => setGrowthReportOpen(true)}>
            <Text style={styles.growthEntryText}>成长报告</Text>
          </Pressable>

          {growthReportOpen ? (
            <Pressable style={styles.reportOverlay} onPress={() => setGrowthReportOpen(false)}>
              <View style={styles.reportPaper}>
                <Text style={styles.reportDoodle}>✿</Text>
                <Text style={styles.reportTitle}>陪6温暖成长</Text>
                <Text style={styles.reportDay}>第<Text style={styles.reportDayNumber}>1</Text>天</Text>
                <View style={styles.reportLine}>
                  <Text style={styles.reportCheck}>✓</Text>
                  <Text style={styles.reportText}>认识了好朋友{companion.name}</Text>
                </View>
                <View style={styles.reportDivider} />
                <View style={styles.reportLine}>
                  <Text style={styles.reportCheck}>✓</Text>
                  <Text style={styles.reportText}>感谢信任，这是一个好开始！</Text>
                </View>
                <Text style={styles.reportHeart}>♡</Text>
                <View style={styles.reportAnimal}>
                  <IdleAnimal companion={companion} />
                </View>
              </View>
            </Pressable>
          ) : null}
        </View>
      </SafeAreaView>
    );
  }

  if (!hydrated) {
    return (
      <SafeAreaView style={styles.safeArea}>
        <StatusBar style="dark" />
        <View style={styles.splash}>
          <View style={styles.splashLeaves} />
          <Text style={styles.splashTitle}>林间聊愈室</Text>
          <Text style={styles.splashSub}>懂你情绪，陪你变好的AI伙伴</Text>
          <View style={styles.splashAnimals}>
            <AnimalBadge companion={companions[1]} large />
            <AnimalBadge companion={companions[2]} large />
          </View>
          <ActivityIndicator color="#ffffff" />
        </View>
      </SafeAreaView>
    );
  }

  if (roomOpen) {
    return renderChatRoom();
  }

  if (detailCompanionId) {
    return renderCompanionDetail(detailCompanion, () => setDetailCompanionId(null), () => {
      setCompanionId(detailCompanion.id);
      setDetailCompanionId(null);
    });
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="dark" />
      <NavigationContainer>
        <Tab.Navigator
          screenOptions={({ route }) => ({
            headerShown: false,
            tabBarActiveTintColor: '#28b6a9',
            tabBarInactiveTintColor: '#9aa7a2',
            tabBarLabelStyle: styles.tabLabel,
            tabBarStyle: styles.tabBar,
            tabBarHideOnKeyboard: true,
            tabBarIcon: ({ color }) => <Text style={[styles.tabIcon, { color }]}>{tabIcon(route.name)}</Text>,
          })}
        >
          <Tab.Screen name="Heal" options={{ tabBarLabel: '聊愈' }}>
            {renderHeal}
          </Tab.Screen>
          <Tab.Screen name="Feed" options={{ tabBarLabel: '碎念' }}>
            {renderFeed}
          </Tab.Screen>
          <Tab.Screen name="Diary" options={{ tabBarLabel: '日记' }}>
            {renderDiary}
          </Tab.Screen>
          <Tab.Screen name="Memory" options={{ tabBarLabel: '记忆' }}>
            {renderMemory}
          </Tab.Screen>
        </Tab.Navigator>
      </NavigationContainer>
    </SafeAreaView>
  );
}

function ScreenFrame({ children, tone }: { children: ReactNode; tone: 'meadow' | 'paper' | 'night' }) {
  return (
    <ScrollView
      style={[styles.screen, tone === 'night' && styles.screenNight]}
      contentContainerStyle={[styles.content, tone === 'paper' && styles.contentPaper, tone === 'night' && styles.contentNight]}
      showsVerticalScrollIndicator={false}
    >
      {children}
    </ScrollView>
  );
}

function renderCompanionDetail(companion: Companion, onBack: () => void, onChoose: () => void) {
  return (
    <SafeAreaView style={[styles.detailSafe, { backgroundColor: detailTone(companion.id) }]}>
      <StatusBar style="dark" />
      <View style={styles.detailCloudOne} />
      <View style={styles.detailCloudTwo} />
      <Pressable style={styles.detailBack} onPress={onBack}>
        <Text style={styles.detailBackText}>‹</Text>
      </Pressable>

      <View style={styles.detailHero}>
        <IdleAnimal companion={companion} large />
        <Text style={styles.detailSparkleLeft}>✦</Text>
        <Text style={styles.detailSparkleRight}>✿</Text>
        <Text style={styles.detailMusic}>♪</Text>
      </View>

      <Text style={styles.detailName}>{companion.name}</Text>

      <View style={styles.outfitRow}>
        {['默认', '新装', '冬日', '花园'].map((item, index) => (
          <View key={`${companion.id}-${item}`} style={[styles.outfitBubble, index === 0 && styles.outfitActive]}>
            <AnimalBadge companion={companion} />
            {index > 0 ? <Text style={styles.newBadge}>新</Text> : <Text style={styles.checkBadge}>✓</Text>}
          </View>
        ))}
      </View>

      <Text style={styles.detailDesc}>{companionIntro(companion.id)}</Text>

      <Pressable style={styles.chooseButton} onPress={onChoose}>
        <Text style={styles.chooseButtonText}>选择Ta</Text>
      </Pressable>
    </SafeAreaView>
  );
}

function MeadowDecor() {
  return (
    <>
      <View style={[styles.flower, styles.flowerLeft]}>
        <Text style={styles.flowerText}>✿</Text>
      </View>
      <View style={[styles.flower, styles.flowerRight]}>
        <Text style={styles.flowerText}>✿</Text>
      </View>
      <View style={styles.mushroomGroup}>
        <Text style={styles.mushroom}>●</Text>
        <Text style={styles.mushroomSmall}>●</Text>
      </View>
      <View style={styles.grassBladeOne} />
      <View style={styles.grassBladeTwo} />
      <View style={styles.grassBladeThree} />
      <FloatingMood style={styles.moodFloatOne} mark="困" />
      <FloatingMood style={styles.moodFloatTwo} mark="累" />
      <FloatingMood style={styles.moodFloatThree} mark="好" />
    </>
  );
}

function FloatingMood({ mark, style }: { mark: string; style: ViewStyle }) {
  const float = useLoopedValue(1600, mark.charCodeAt(0) * 7);
  const translateY = float.interpolate({ inputRange: [0, 1], outputRange: [0, -8] });
  const opacity = float.interpolate({ inputRange: [0, 1], outputRange: [0.56, 1] });

  return (
    <Animated.View style={[styles.moodFloat, style, { opacity, transform: [{ translateY }] }]}>
      <Text style={styles.moodFloatText}>{mark}</Text>
    </Animated.View>
  );
}

function RoomDecor() {
  return (
    <>
      <View style={styles.roomWindowShape} />
      <View style={styles.roomStumpOne} />
      <View style={styles.roomStumpTwo} />
      <View style={styles.roomBucket}>
        <Text style={styles.roomBucketText}>0%</Text>
      </View>
      <SnowFlake style={styles.snowOne} delay={0} />
      <SnowFlake style={styles.snowTwo} delay={330} />
      <SnowFlake style={styles.snowThree} delay={680} />
    </>
  );
}

function SnowFlake({ style, delay }: { style: ViewStyle; delay: number }) {
  const fall = useLoopedValue(1400, delay);
  const translateY = fall.interpolate({ inputRange: [0, 1], outputRange: [-8, 16] });
  const opacity = fall.interpolate({ inputRange: [0, 0.5, 1], outputRange: [0.2, 0.9, 0.2] });

  return (
    <Animated.Text style={[styles.snowText, style, { opacity, transform: [{ translateY }] }]}>✦</Animated.Text>
  );
}

function AnimalBadge({ companion, large = false }: { companion: Companion; large?: boolean }) {
  return (
    <View style={[styles.animalBadge, large && styles.animalBadgeLarge, { backgroundColor: companion.soft }]}>
      <View style={[styles.animalFace, large && styles.animalFaceLarge, { backgroundColor: companion.color }]}>
        <Text style={[styles.animalMark, large && styles.animalMarkLarge]}>{companion.mark}</Text>
      </View>
    </View>
  );
}

function AnimalScene({ companion, style, onPress }: { companion: Companion; style: ViewStyle; onPress?: () => void }) {
  const idle = useLoopedValue(1900, companion.id === 'fox' ? 0 : companion.id === 'bear' ? 260 : 520);
  const translateY = idle.interpolate({ inputRange: [0, 1], outputRange: [0, -7] });
  const rotate = idle.interpolate({ inputRange: [0, 1], outputRange: ['-2deg', '2deg'] });

  return (
    <Animated.View style={[styles.animalScene, style, { transform: [{ translateY }, { rotate }] }]}>
      <Pressable onPress={onPress} style={styles.animalPressable}>
      <IdleAnimal companion={companion} large />
      <Text style={styles.animalSceneName}>{companion.name}</Text>
      </Pressable>
    </Animated.View>
  );
}

function IdleAnimal({ companion, large = false }: { companion: Companion; large?: boolean }) {
  const float = useLoopedValue(1500, companion.id === 'fox' ? 100 : companion.id === 'deer' ? 420 : 760);
  const bubble = useLoopedValue(2100, companion.id === 'fox' ? 0 : companion.id === 'deer' ? 360 : 720);
  const translateY = float.interpolate({ inputRange: [0, 1], outputRange: [0, -5] });
  const scale = float.interpolate({ inputRange: [0, 1], outputRange: [1, 1.04] });
  const bubbleOpacity = bubble.interpolate({ inputRange: [0, 0.35, 0.75, 1], outputRange: [0, 1, 1, 0] });
  const bubbleRise = bubble.interpolate({ inputRange: [0, 1], outputRange: [8, -12] });

  return (
    <Animated.View style={[styles.idleAnimal, { transform: [{ translateY }, { scale }] }]}>
      <SpriteAnimal companion={companion} large={large} />
      <Animated.View style={[styles.emotionBubble, { opacity: bubbleOpacity, transform: [{ translateY: bubbleRise }] }]}>
        <Text style={styles.emotionText}>{emotionMark(companion.id)}</Text>
      </Animated.View>
    </Animated.View>
  );
}

function SpriteAnimal({ companion, large = false }: { companion: Companion; large?: boolean }) {
  const frames = spriteFrames[companion.id];
  const [frameIndex, setFrameIndex] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setFrameIndex((prev) => (prev + 1) % frames.length);
    }, 150);

    return () => clearInterval(timer);
  }, [frames.length]);

  return (
    <Image
      source={frames[frameIndex]}
      style={[styles.spriteAnimal, large && styles.spriteAnimalLarge]}
      resizeMode="contain"
    />
  );
}

function HealButton({ onPress }: { onPress: () => void }) {
  const pulse = useLoopedValue(1200);
  const ringScale = pulse.interpolate({ inputRange: [0, 1], outputRange: [0.86, 1.28] });
  const ringOpacity = pulse.interpolate({ inputRange: [0, 1], outputRange: [0.5, 0] });
  const buttonScale = pulse.interpolate({ inputRange: [0, 1], outputRange: [1, 1.04] });

  return (
    <Animated.View style={[styles.healButton, { transform: [{ scale: buttonScale }] }]}>
      <Animated.View style={[styles.healPulseRing, { opacity: ringOpacity, transform: [{ scale: ringScale }] }]} />
      <Pressable style={styles.healButtonCore} onPress={onPress}>
        <Text style={styles.healButtonText}>聊愈</Text>
      </Pressable>
    </Animated.View>
  );
}

function TwinkleStar({ point, index }: { point: { left: DimensionValue; top: DimensionValue }; index: number }) {
  const twinkle = useLoopedValue(900 + index * 110, index * 170);
  const opacity = twinkle.interpolate({ inputRange: [0, 1], outputRange: [0.25, 1] });
  const scale = twinkle.interpolate({ inputRange: [0, 1], outputRange: [0.7, 1.45] });

  return (
    <Animated.View
      style={[styles.starPoint, point, index % 2 === 0 && styles.starPointLarge, { opacity, transform: [{ scale }] }]}
    />
  );
}

function VoiceBars() {
  const pulse = useLoopedValue(720);
  const opacity = pulse.interpolate({ inputRange: [0, 1], outputRange: [0.45, 1] });
  const translateY = pulse.interpolate({ inputRange: [0, 1], outputRange: [4, -4] });

  return (
    <Animated.Text style={[styles.voiceBars, { opacity, transform: [{ translateY }] }]}>▂▅▇▅▂</Animated.Text>
  );
}

function PostCard({ post, author }: { post: FeedPost; author: Companion }) {
  return (
    <View style={styles.postCard}>
      <View style={styles.postHead}>
        <AnimalBadge companion={author} />
        <View>
          <Text style={styles.postAuthor}>{author.name}</Text>
          <Text style={styles.postTime}>{post.time}</Text>
        </View>
      </View>
      <Text style={styles.postTitle}>{post.title}</Text>
      <Text style={styles.postBody}>{post.body}</Text>
      {post.imageLabel ? (
        <View style={styles.postImage}>
          <Text style={styles.postImageText}>{post.imageLabel}</Text>
          <View style={styles.postImageShine} />
        </View>
      ) : null}
      <View style={styles.reactionRow}>
        <Text style={styles.reactionChip}>♡ 12</Text>
        <Text style={styles.reactionChip}>💬 {post.comments.length}</Text>
        <Text style={styles.reactionChip}>收藏夹减压 +1</Text>
      </View>
      <View style={styles.commentBox}>
        {post.comments.map((comment) => (
          <Text key={comment} style={styles.commentText}>{comment}</Text>
        ))}
      </View>
    </View>
  );
}

function MoodChart({ values }: { values: number[] }) {
  return (
    <View style={styles.chart}>
      {values.map((value, index) => (
        <View key={`${value}-${index}`} style={styles.chartColumn}>
          <View style={[styles.chartStem, { height: value }]} />
          <View style={[styles.chartDot, index === values.length - 1 && styles.chartDotActive]} />
          <Text style={styles.chartDay}>{13 + index}</Text>
        </View>
      ))}
    </View>
  );
}

function IconButton({ title, mark }: { title: string; mark: string }) {
  return (
    <View style={styles.iconButton}>
      <Text style={styles.iconButtonMark}>{mark}</Text>
      <Text style={styles.iconButtonText}>{title}</Text>
    </View>
  );
}

function PrimaryButton({ label, onPress }: { label: string; onPress: () => void }) {
  return (
    <Pressable style={styles.primaryButton} onPress={onPress}>
      <Text style={styles.primaryButtonText}>{label}</Text>
    </Pressable>
  );
}

function GhostButton({ label, onPress }: { label: string; onPress: () => void }) {
  return (
    <Pressable style={styles.ghostButton} onPress={onPress}>
      <Text style={styles.ghostButtonText}>{label}</Text>
    </Pressable>
  );
}

function buildPostcard(note: NoteEntry, companion: Companion) {
  return `${companion.name}给你寄来明信片：${note.text} 你不是没有进步，只是正在用很小的方式重新恢复能量。`;
}

function detailTone(id: CompanionId) {
  if (id === 'fox') return '#d9f6ef';
  if (id === 'bear') return '#fff3c8';
  return '#ffe1e4';
}

function companionIntro(id: CompanionId) {
  if (id === 'fox') {
    return '敏锐又深刻的小狐狸，喜欢摩托车和种花，特别是思维玫瑰。总能一针见血地洞察心底的真正想法。帮你拨开迷雾、看清自己';
  }
  if (id === 'bear') {
    return '笨笨又暖心的胖棕熊，超喜欢美食和烹饪。温暖纯粹的他，能用细腻清澈的视角，陪你看见内心本来的样子';
  }
  return '元气满满、明亮坚定的女孩，喜欢阅读和画画。你最坚定的守护者！无论什么时候都站在你身边，陪你对抗坏情绪';
}

function feedStickerTitle(id: CompanionId) {
  if (id === 'fox') return '▦ 数据打脸';
  if (id === 'deer') return '☔ 内心下雨';
  return '🙌 火速叛变';
}

function buildPostTitle(body: string) {
  const clean = body.replace(/\s+/g, ' ').trim();
  if (clean.includes('收藏')) return '收藏夹又多了一只心事怪';
  if (clean.includes('累') || clean.includes('疲惫')) return '今天的能量条只剩一点点';
  if (clean.includes('焦虑') || clean.includes('紧张')) return '脑袋里开了一场小型暴风雨';
  return clean.length > 16 ? `${clean.slice(0, 16)}...` : clean;
}

function buildRoomReply(text: string, companion: Companion, trouble: TroubleOption) {
  if (text.includes('累') || text.includes('疲惫')) {
    return `${companion.name}听到了。今天先不用证明自己很有能量，我们只做一件小事：把肩膀放低，再喝一口水。`;
  }
  if (text.includes('焦虑') || text.includes('紧张') || text.includes('乱')) {
    return `这像是“${trouble.title}”在敲门。先把它放到门外，我们一起数三次呼吸，再决定下一步。`;
  }
  if (text.includes('不知道') || text.includes('迷茫')) {
    return `不用马上找到答案。${companion.name}建议你先写下一个很小的方向：现在什么能让你轻一点？`;
  }
  if (text.includes('收藏') || text.includes('视频')) {
    return `花花狸已经把“收藏夹自动学会术”列入荒诞传说。我们先选一个最短的视频，只看三分钟。`;
  }
  return `${companion.name}在这里。你说得越具体，我越能陪你把这团心事拆成能处理的小块。`;
}

function nextTroubleId(currentId: string) {
  const currentIndex = troubleOptions.findIndex((item) => item.id === currentId);
  const nextIndex = currentIndex < 0 ? 0 : (currentIndex + 1) % troubleOptions.length;
  return troubleOptions[nextIndex].id;
}

function latestRoomUserText(messages: RoomMessage[]) {
  return [...messages].reverse().find((message) => message.role === 'user')?.text ?? '';
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '刚刚';
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

function tabIcon(name: string) {
  if (name === 'Heal') return '聊';
  if (name === 'Feed') return '碎';
  if (name === 'Diary') return '记';
  return '星';
}

function emotionMark(id: CompanionId) {
  if (id === 'fox') return '♪';
  if (id === 'deer') return '♡';
  return '…';
}

function useLoopedValue(duration: number, delay = 0) {
  const value = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const animation = Animated.loop(
      Animated.sequence([
        Animated.delay(delay),
        Animated.timing(value, {
          toValue: 1,
          duration,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
        Animated.timing(value, {
          toValue: 0,
          duration,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
      ])
    );
    animation.start();
    return () => animation.stop();
  }, [delay, duration, value]);

  return value;
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#dff3e3',
  },
  detailSafe: {
    flex: 1,
    paddingHorizontal: 24,
    overflow: 'hidden',
  },
  detailCloudOne: {
    position: 'absolute',
    right: -50,
    top: 96,
    width: 190,
    height: 120,
    borderRadius: 999,
    backgroundColor: 'rgba(255,255,255,0.28)',
  },
  detailCloudTwo: {
    position: 'absolute',
    left: -38,
    top: 470,
    width: 240,
    height: 110,
    borderRadius: 999,
    backgroundColor: 'rgba(255,255,255,0.36)',
  },
  detailBack: {
    width: 58,
    height: 58,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 34,
  },
  detailBackText: {
    color: '#27302e',
    fontSize: 58,
    lineHeight: 58,
    fontWeight: '700',
  },
  detailHero: {
    height: 390,
    alignItems: 'center',
    justifyContent: 'center',
  },
  detailSparkleLeft: {
    position: 'absolute',
    left: 72,
    top: 92,
    color: '#f8c85b',
    fontSize: 34,
    fontWeight: '900',
  },
  detailSparkleRight: {
    position: 'absolute',
    right: 62,
    top: 156,
    color: '#72b7ed',
    fontSize: 28,
    fontWeight: '900',
  },
  detailMusic: {
    position: 'absolute',
    right: 86,
    bottom: 70,
    color: 'rgba(255,255,255,0.96)',
    fontSize: 28,
    fontWeight: '900',
  },
  detailName: {
    color: '#2d3533',
    fontSize: 34,
    textAlign: 'center',
    fontWeight: '900',
    marginBottom: 22,
  },
  outfitRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 28,
  },
  outfitBubble: {
    width: 72,
    height: 72,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.82)',
    shadowColor: '#5a7770',
    shadowOpacity: 0.14,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 5 },
    elevation: 4,
  },
  outfitActive: {
    borderWidth: 4,
    borderColor: '#16bdb4',
  },
  checkBadge: {
    position: 'absolute',
    right: -4,
    top: -4,
    width: 26,
    height: 26,
    borderRadius: 999,
    overflow: 'hidden',
    textAlign: 'center',
    textAlignVertical: 'center',
    color: '#ffffff',
    backgroundColor: '#16bdb4',
    fontWeight: '900',
  },
  newBadge: {
    position: 'absolute',
    right: -6,
    top: -8,
    paddingHorizontal: 5,
    paddingVertical: 2,
    borderRadius: 6,
    overflow: 'hidden',
    color: '#ffffff',
    backgroundColor: '#ff5a24',
    fontSize: 12,
    fontWeight: '900',
  },
  detailDesc: {
    color: '#515f5b',
    fontSize: 22,
    lineHeight: 40,
    fontWeight: '700',
  },
  chooseButton: {
    alignSelf: 'center',
    position: 'absolute',
    left: 70,
    right: 70,
    bottom: 56,
    height: 68,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#10b9ad',
    shadowColor: '#2a6d68',
    shadowOpacity: 0.24,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 6 },
    elevation: 8,
  },
  chooseButtonText: {
    color: '#ffffff',
    fontSize: 24,
    fontWeight: '900',
  },
  screen: {
    flex: 1,
    backgroundColor: '#dff3e3',
  },
  screenNight: {
    backgroundColor: '#071a34',
  },
  content: {
    minHeight: '100%',
    paddingHorizontal: 18,
    paddingTop: 18,
    paddingBottom: 118,
    backgroundColor: '#dff3e3',
  },
  contentPaper: {
    backgroundColor: '#f7fbfb',
  },
  contentNight: {
    backgroundColor: '#071a34',
  },
  splash: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
    backgroundColor: '#8bc9ec',
  },
  splashLeaves: {
    position: 'absolute',
    left: -40,
    top: -20,
    width: 160,
    height: 160,
    borderRadius: 80,
    backgroundColor: 'rgba(93,151,83,0.35)',
  },
  splashTitle: {
    color: '#ffffff',
    fontSize: 42,
    fontWeight: '900',
    letterSpacing: 2,
  },
  splashSub: {
    color: '#f8ffff',
    fontSize: 16,
    fontWeight: '800',
    marginTop: 12,
    marginBottom: 72,
  },
  splashAnimals: {
    flexDirection: 'row',
    gap: 26,
    marginBottom: 34,
  },
  homeTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 42,
  },
  timerPill: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 22,
    overflow: 'hidden',
    backgroundColor: 'rgba(255,255,255,0.78)',
  },
  timerIcon: {
    paddingLeft: 12,
    color: '#536b65',
    fontSize: 18,
  },
  timerText: {
    paddingHorizontal: 8,
    paddingVertical: 10,
    color: '#2f5148',
    fontSize: 18,
    fontWeight: '800',
  },
  timerPlus: {
    paddingHorizontal: 14,
    paddingVertical: 10,
    color: '#ffffff',
    backgroundColor: '#91ddd4',
    fontSize: 22,
    fontWeight: '900',
  },
  restoreText: {
    color: '#8aa29b',
    marginTop: 8,
    marginLeft: 16,
  },
  avatarBubble: {
    width: 54,
    height: 54,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 3,
    borderColor: 'rgba(255,255,255,0.8)',
    backgroundColor: '#d8c3ad',
  },
  avatarText: {
    color: '#694f43',
    fontWeight: '900',
  },
  homeGreeting: {
    color: '#42594f',
    fontSize: 21,
    lineHeight: 30,
    textAlign: 'center',
    marginBottom: 18,
  },
  meadowStage: {
    minHeight: 610,
    marginBottom: 18,
    borderRadius: 38,
    overflow: 'hidden',
    backgroundColor: '#bde7bf',
  },
  cloudOne: {
    position: 'absolute',
    right: -45,
    top: -30,
    width: 180,
    height: 92,
    borderRadius: 70,
    backgroundColor: 'rgba(255,255,255,0.46)',
  },
  cloudTwo: {
    position: 'absolute',
    left: -30,
    top: 42,
    width: 150,
    height: 76,
    borderRadius: 60,
    backgroundColor: 'rgba(255,255,255,0.32)',
  },
  stageCallout: {
    position: 'absolute',
    left: 42,
    top: 42,
    maxWidth: 230,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 18,
    backgroundColor: 'rgba(255,255,255,0.72)',
  },
  stageCalloutText: {
    color: '#587168',
    fontSize: 12,
    fontWeight: '800',
    lineHeight: 18,
  },
  flower: {
    position: 'absolute',
    width: 42,
    height: 42,
    alignItems: 'center',
    justifyContent: 'center',
  },
  flowerLeft: {
    left: 20,
    bottom: 32,
  },
  flowerRight: {
    right: 26,
    bottom: 46,
  },
  flowerText: {
    color: '#fff5d6',
    fontSize: 28,
    textShadowColor: 'rgba(84,121,93,0.35)',
    textShadowRadius: 5,
  },
  mushroomGroup: {
    position: 'absolute',
    right: 86,
    top: 234,
    flexDirection: 'row',
    gap: 4,
  },
  mushroom: {
    color: '#e17e55',
    fontSize: 18,
  },
  mushroomSmall: {
    color: '#d7644d',
    fontSize: 12,
    marginTop: 7,
  },
  grassBladeOne: {
    position: 'absolute',
    left: 68,
    bottom: 90,
    width: 6,
    height: 42,
    borderRadius: 999,
    backgroundColor: 'rgba(76,151,99,0.45)',
    transform: [{ rotate: '-18deg' }],
  },
  grassBladeTwo: {
    position: 'absolute',
    left: 86,
    bottom: 82,
    width: 5,
    height: 35,
    borderRadius: 999,
    backgroundColor: 'rgba(76,151,99,0.4)',
    transform: [{ rotate: '14deg' }],
  },
  grassBladeThree: {
    position: 'absolute',
    right: 76,
    bottom: 94,
    width: 6,
    height: 46,
    borderRadius: 999,
    backgroundColor: 'rgba(76,151,99,0.42)',
    transform: [{ rotate: '18deg' }],
  },
  moodFloat: {
    position: 'absolute',
    width: 34,
    height: 34,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.72)',
  },
  moodFloatOne: {
    left: 88,
    top: 190,
  },
  moodFloatTwo: {
    right: 86,
    top: 212,
  },
  moodFloatThree: {
    right: 128,
    top: 304,
  },
  moodFloatText: {
    color: '#6a8c7e',
    fontSize: 12,
    fontWeight: '900',
  },
  animalScene: {
    position: 'absolute',
    alignItems: 'center',
  },
  animalPressable: {
    alignItems: 'center',
  },
  foxSpot: {
    left: 28,
    top: 185,
  },
  bearSpot: {
    right: 26,
    top: 156,
  },
  deerSpot: {
    right: 48,
    top: 342,
  },
  animalSceneName: {
    color: '#527064',
    fontWeight: '900',
    marginTop: 6,
  },
  idleAnimal: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  spriteAnimal: {
    width: 76,
    height: 88,
  },
  spriteAnimalLarge: {
    width: 126,
    height: 138,
  },
  emotionBubble: {
    position: 'absolute',
    right: -10,
    top: -8,
    width: 30,
    height: 30,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.86)',
  },
  emotionText: {
    color: '#55a58c',
    fontSize: 17,
    fontWeight: '900',
  },
  picnicMat: {
    position: 'absolute',
    left: '40%',
    top: 286,
    width: 86,
    height: 70,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#8b5e45',
  },
  picnicText: {
    color: '#fff1d8',
    fontSize: 24,
    fontWeight: '900',
  },
  snowGlobe: {
    position: 'absolute',
    left: 50,
    bottom: 150,
    width: 106,
    height: 88,
    borderRadius: 52,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 3,
    borderColor: 'rgba(255,255,255,0.66)',
    backgroundColor: 'rgba(197,247,242,0.54)',
  },
  snowGlobeText: {
    color: '#207f77',
    fontWeight: '900',
  },
  healButton: {
    position: 'absolute',
    alignSelf: 'center',
    bottom: 24,
    width: 116,
    height: 116,
    alignItems: 'center',
    justifyContent: 'center',
  },
  healPulseRing: {
    position: 'absolute',
    width: 116,
    height: 116,
    borderRadius: 999,
    borderWidth: 2,
    borderColor: '#ffffff',
    backgroundColor: 'rgba(255,255,255,0.16)',
  },
  healButtonCore: {
    width: 116,
    height: 116,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 8,
    borderColor: 'rgba(255,255,255,0.48)',
    backgroundColor: '#28b6a9',
  },
  healButtonText: {
    color: '#ffffff',
    fontSize: 24,
    fontWeight: '900',
  },
  companionStrip: {
    gap: 10,
  },
  tapHint: {
    color: '#668179',
    textAlign: 'center',
    fontWeight: '800',
    marginTop: 2,
  },
  companionCard: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    padding: 12,
    borderRadius: 24,
    borderWidth: 2,
    borderColor: 'transparent',
    backgroundColor: 'rgba(255,255,255,0.72)',
  },
  companionName: {
    color: '#304a43',
    fontSize: 17,
    fontWeight: '900',
    marginBottom: 4,
  },
  companionScene: {
    color: '#61766e',
    lineHeight: 19,
  },
  flexFill: {
    flex: 1,
  },
  animalBadge: {
    width: 54,
    height: 54,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  animalBadgeLarge: {
    width: 86,
    height: 86,
    borderRadius: 30,
  },
  animalFace: {
    width: '72%',
    height: '72%',
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
  },
  animalFaceLarge: {
    width: '74%',
    height: '74%',
  },
  animalMark: {
    color: '#fffdf2',
    fontSize: 18,
    fontWeight: '900',
  },
  animalMarkLarge: {
    fontSize: 30,
  },
  feedHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  bubbleMenu: {
    width: 46,
    height: 46,
    borderRadius: 999,
    textAlign: 'center',
    textAlignVertical: 'center',
    color: '#67827b',
    backgroundColor: 'rgba(255,255,255,0.72)',
    fontSize: 22,
    fontWeight: '900',
  },
  avatarBubbleSmall: {
    width: 46,
    height: 46,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#d8c3ad',
  },
  avatarTextSmall: {
    color: '#6b5142',
    fontWeight: '900',
  },
  storyBoard: {
    padding: 14,
    marginBottom: 12,
    borderRadius: 30,
    backgroundColor: 'rgba(255,255,255,0.75)',
  },
  stickerRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 12,
  },
  stickerCard: {
    flex: 1,
    alignItems: 'center',
    gap: 6,
    paddingVertical: 10,
    borderRadius: 4,
    borderWidth: 4,
    borderColor: '#ffffff',
    backgroundColor: '#80d074',
    shadowColor: '#355b45',
    shadowOpacity: 0.18,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 4 },
    elevation: 4,
  },
  stickerText: {
    color: '#348b79',
    fontSize: 12,
    fontWeight: '900',
  },
  eventCard: {
    flexDirection: 'row',
    gap: 12,
    alignItems: 'center',
    padding: 12,
    borderRadius: 24,
    backgroundColor: '#f6fbef',
  },
  fakePhoto: {
    width: 120,
    height: 78,
    borderRadius: 2,
    borderWidth: 6,
    borderColor: '#ffffff',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#e8c9a1',
    overflow: 'hidden',
  },
  fakePhotoText: {
    color: '#754c35',
    fontWeight: '900',
  },
  fakePhotoSub: {
    color: '#8a654b',
    fontSize: 10,
    marginTop: 4,
  },
  eventTitle: {
    color: '#33463f',
    fontSize: 17,
    lineHeight: 24,
    fontWeight: '900',
  },
  eventDate: {
    color: '#88958f',
    marginTop: 12,
  },
  broadcast: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    padding: 12,
    marginBottom: 12,
    borderRadius: 999,
    backgroundColor: 'rgba(255,255,255,0.82)',
  },
  broadcastIcon: {
    color: '#28b6a9',
    fontWeight: '900',
  },
  broadcastText: {
    flex: 1,
    color: '#60716b',
  },
  postCard: {
    padding: 16,
    marginBottom: 14,
    borderRadius: 28,
    backgroundColor: '#ffffff',
  },
  postHead: {
    flexDirection: 'row',
    gap: 10,
    alignItems: 'center',
    marginBottom: 12,
  },
  postAuthor: {
    color: '#28a993',
    fontSize: 17,
    fontWeight: '900',
  },
  postTime: {
    color: '#9ba8a2',
    marginTop: 2,
  },
  postTitle: {
    color: '#314a42',
    fontSize: 18,
    fontWeight: '900',
    marginBottom: 8,
  },
  postBody: {
    color: '#34443f',
    fontSize: 16,
    lineHeight: 27,
  },
  postImage: {
    height: 180,
    borderRadius: 22,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 12,
    backgroundColor: '#bfdcae',
    overflow: 'hidden',
  },
  postImageText: {
    color: '#fffdf5',
    fontSize: 24,
    fontWeight: '900',
  },
  postImageShine: {
    position: 'absolute',
    left: -40,
    top: -20,
    width: 120,
    height: 240,
    backgroundColor: 'rgba(255,255,255,0.22)',
    transform: [{ rotate: '24deg' }],
  },
  reactionRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginTop: 12,
  },
  reactionChip: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 999,
    overflow: 'hidden',
    color: '#5a756e',
    backgroundColor: '#eef8f5',
    fontSize: 12,
    fontWeight: '800',
  },
  feedComposer: {
    padding: 16,
    marginBottom: 14,
    borderRadius: 28,
    backgroundColor: 'rgba(255,255,255,0.86)',
  },
  feedComposerTitle: {
    color: '#314a42',
    fontSize: 17,
    fontWeight: '900',
    marginBottom: 10,
  },
  feedInput: {
    minHeight: 86,
    padding: 12,
    borderRadius: 18,
    color: '#263d36',
    backgroundColor: '#f4fbf8',
    textAlignVertical: 'top',
    marginBottom: 12,
  },
  commentBox: {
    gap: 7,
    padding: 12,
    marginTop: 12,
    borderRadius: 20,
    backgroundColor: '#f4fbf8',
  },
  commentText: {
    color: '#4b635d',
    lineHeight: 21,
  },
  floatPlus: {
    position: 'absolute',
    right: 20,
    bottom: 126,
    width: 58,
    height: 58,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#77dcd4',
    shadowColor: '#45aca5',
    shadowOpacity: 0.28,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 8 },
    elevation: 8,
  },
  floatPlusText: {
    color: '#ffffff',
    fontSize: 34,
    fontWeight: '800',
  },
  segment: {
    alignSelf: 'center',
    flexDirection: 'row',
    padding: 4,
    marginBottom: 20,
    borderRadius: 999,
    backgroundColor: '#d7e0dd',
  },
  segmentMuted: {
    paddingHorizontal: 18,
    paddingVertical: 10,
    color: '#ffffff',
    fontWeight: '900',
  },
  segmentActive: {
    paddingHorizontal: 24,
    paddingVertical: 10,
    borderRadius: 999,
    overflow: 'hidden',
    color: '#2f473f',
    backgroundColor: '#ffffff',
    fontWeight: '900',
  },
  weekCard: {
    padding: 18,
    marginBottom: 18,
    borderRadius: 28,
    backgroundColor: '#ffffff',
  },
  cardHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  paperTitle: {
    color: '#263b35',
    fontSize: 19,
    fontWeight: '900',
  },
  calendarChip: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 999,
    overflow: 'hidden',
    color: '#51635d',
    backgroundColor: '#f2f5f3',
    fontWeight: '900',
  },
  weekRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  weekDay: {
    alignItems: 'center',
    gap: 7,
  },
  weekDot: {
    width: 2,
    height: 48,
    backgroundColor: '#e8eeee',
  },
  weekDotActive: {
    width: 26,
    height: 26,
    borderRadius: 999,
    backgroundColor: '#42c6b7',
  },
  weekText: {
    color: '#8b9892',
    lineHeight: 22,
    textAlign: 'center',
  },
  weekTextActive: {
    color: '#42b6a9',
    fontWeight: '900',
  },
  chartCard: {
    padding: 18,
    marginBottom: 18,
    borderRadius: 30,
    borderWidth: 6,
    borderColor: '#83ded4',
    backgroundColor: '#ffffff',
  },
  chartTitle: {
    color: '#334a43',
    fontWeight: '900',
  },
  chartSub: {
    color: '#7fb7a8',
    marginTop: 4,
  },
  chart: {
    height: 116,
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    marginBottom: 18,
  },
  chartColumn: {
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: 5,
  },
  chartStem: {
    width: 4,
    borderRadius: 999,
    backgroundColor: '#cbeee8',
  },
  chartDot: {
    width: 13,
    height: 13,
    borderRadius: 999,
    backgroundColor: '#f6a86e',
  },
  chartDotActive: {
    backgroundColor: '#40c8b8',
  },
  chartDay: {
    color: '#8e9d97',
    fontSize: 12,
  },
  goalTitle: {
    color: '#384b44',
    fontSize: 17,
    fontWeight: '900',
    marginBottom: 10,
  },
  notePreview: {
    padding: 14,
    borderRadius: 18,
    color: '#5d6e68',
    lineHeight: 22,
    backgroundColor: '#fff8d9',
    marginBottom: 14,
  },
  doorCard: {
    minHeight: 560,
    padding: 20,
    borderRadius: 34,
    overflow: 'hidden',
    backgroundColor: '#2f3856',
  },
  doorQuestion: {
    color: '#ffffff',
    fontSize: 21,
    lineHeight: 30,
    textAlign: 'center',
    fontWeight: '900',
    marginTop: 8,
    marginBottom: 20,
  },
  magicDoor: {
    alignSelf: 'center',
    width: 112,
    height: 160,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 3,
    borderColor: '#f7d68d',
    backgroundColor: '#775b62',
    shadowColor: '#f8d996',
    shadowOpacity: 0.55,
    shadowRadius: 20,
    shadowOffset: { width: 0, height: 0 },
    elevation: 8,
    marginBottom: 18,
  },
  doorLight: {
    color: '#fff0c6',
    fontSize: 38,
    fontWeight: '900',
  },
  troubleOption: {
    padding: 14,
    marginBottom: 12,
    borderRadius: 16,
    backgroundColor: 'rgba(255,255,255,0.88)',
  },
  troubleOptionActive: {
    backgroundColor: '#fff7e6',
    borderWidth: 2,
    borderColor: '#f1c16f',
  },
  troubleTitle: {
    color: '#2d3f3a',
    fontSize: 16,
    fontWeight: '900',
    marginBottom: 5,
  },
  troubleDetail: {
    color: '#586864',
    lineHeight: 21,
  },
  resultTags: {
    gap: 8,
    marginTop: 6,
  },
  resultTag: {
    color: '#ffffff',
    fontSize: 20,
    lineHeight: 32,
    fontWeight: '900',
    transform: [{ rotate: '-7deg' }],
  },
  doorActions: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 18,
  },
  starMap: {
    height: 760,
    marginHorizontal: -18,
    marginTop: -18,
    marginBottom: 18,
    overflow: 'hidden',
    backgroundColor: '#071a34',
  },
  nebulaOne: {
    position: 'absolute',
    left: -80,
    top: 80,
    width: 260,
    height: 260,
    borderRadius: 999,
    backgroundColor: 'rgba(79,132,190,0.18)',
  },
  nebulaTwo: {
    position: 'absolute',
    right: -70,
    bottom: 34,
    width: 240,
    height: 180,
    borderRadius: 999,
    backgroundColor: 'rgba(122,218,206,0.12)',
  },
  starPoint: {
    position: 'absolute',
    width: 4,
    height: 4,
    borderRadius: 999,
    backgroundColor: '#ffffff',
  },
  starPointLarge: {
    width: 6,
    height: 6,
  },
  orbitOuter: {
    position: 'absolute',
    left: '12%',
    top: '22%',
    width: '76%',
    height: '52%',
    borderRadius: 999,
    borderWidth: 1,
    borderColor: 'rgba(236,221,151,0.28)',
    transform: [{ rotate: '-14deg' }],
  },
  orbitInner: {
    position: 'absolute',
    left: '22%',
    top: '32%',
    width: '56%',
    height: '36%',
    borderRadius: 999,
    borderWidth: 1,
    borderColor: 'rgba(236,221,151,0.22)',
    transform: [{ rotate: '-8deg' }],
  },
  userInStars: {
    position: 'absolute',
    left: '43%',
    top: '39%',
    width: 76,
    height: 76,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(246,241,194,0.18)',
  },
  userInStarsText: {
    color: '#fff4c2',
    fontSize: 30,
    fontWeight: '900',
  },
  sideActions: {
    position: 'absolute',
    right: 20,
    top: 40,
    gap: 18,
  },
  iconButton: {
    alignItems: 'center',
    gap: 7,
  },
  iconButtonMark: {
    width: 45,
    height: 45,
    borderRadius: 12,
    overflow: 'hidden',
    textAlign: 'center',
    textAlignVertical: 'center',
    color: '#705b32',
    backgroundColor: '#f5e8bd',
    fontWeight: '900',
  },
  iconButtonText: {
    color: '#f5e8bd',
    fontWeight: '900',
  },
  roomCard: {
    borderRadius: 34,
    overflow: 'hidden',
    marginBottom: 18,
    backgroundColor: '#8fa4b8',
  },
  chatRoomSafe: {
    flex: 1,
    backgroundColor: '#6f859c',
  },
  chatRoomScene: {
    flex: 1,
    overflow: 'hidden',
    backgroundColor: '#7f98ad',
  },
  chatRoomTop: {
    position: 'absolute',
    left: 18,
    right: 18,
    top: 18,
    zIndex: 5,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  chatRoomBack: {
    width: 50,
    height: 50,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.22)',
  },
  chatRoomBackText: {
    color: '#ffffff',
    fontSize: 40,
    lineHeight: 42,
  },
  chatRoomTimer: {
    paddingHorizontal: 28,
    paddingVertical: 13,
    borderRadius: 999,
    overflow: 'hidden',
    color: '#2fb9a9',
    backgroundColor: '#ffffff',
    fontSize: 19,
    fontWeight: '900',
  },
  chatRoomSound: {
    width: 50,
    height: 50,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#ffffff',
  },
  chatRoomSoundText: {
    color: '#2fb9a9',
    fontWeight: '900',
  },
  chatRoomCenter: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingBottom: 96,
  },
  chatRoomShortcuts: {
    position: 'absolute',
    left: 28,
    right: 28,
    bottom: 86,
    zIndex: 4,
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  roomShortcut: {
    width: 74,
    height: 74,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 5,
    backgroundColor: 'rgba(255,255,255,0.34)',
  },
  roomShortcutIcon: {
    color: '#ffffff',
    fontSize: 20,
    fontWeight: '900',
  },
  roomShortcutText: {
    color: '#ffffff',
    fontWeight: '900',
  },
  chatRoomInputDock: {
    position: 'absolute',
    left: 10,
    right: 10,
    bottom: 14,
    zIndex: 5,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    padding: 12,
    borderRadius: 28,
    backgroundColor: '#ffffff',
  },
  growthEntry: {
    position: 'absolute',
    right: 18,
    bottom: 166,
    zIndex: 5,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: 'rgba(255,255,255,0.26)',
  },
  growthEntryText: {
    color: '#ffffff',
    fontSize: 12,
    fontWeight: '900',
  },
  reportOverlay: {
    ...StyleSheet.absoluteFillObject,
    zIndex: 10,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(26,35,42,0.36)',
  },
  reportPaper: {
    width: '84%',
    minHeight: 520,
    borderRadius: 12,
    paddingHorizontal: 34,
    paddingTop: 58,
    backgroundColor: '#fffefe',
    shadowColor: '#000000',
    shadowOpacity: 0.18,
    shadowRadius: 20,
    shadowOffset: { width: 0, height: 10 },
    elevation: 10,
  },
  reportDoodle: {
    position: 'absolute',
    left: -22,
    top: 110,
    color: '#ff7a26',
    fontSize: 52,
    fontWeight: '900',
  },
  reportTitle: {
    color: '#111111',
    fontSize: 28,
    textAlign: 'center',
    fontWeight: '900',
    textDecorationLine: 'underline',
    textDecorationColor: '#58d5c8',
  },
  reportDay: {
    color: '#303534',
    textAlign: 'center',
    fontSize: 25,
    marginTop: 48,
    marginBottom: 36,
  },
  reportDayNumber: {
    color: '#f0a31a',
    fontSize: 42,
    fontWeight: '900',
  },
  reportLine: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
    marginBottom: 18,
  },
  reportCheck: {
    width: 32,
    height: 32,
    borderRadius: 999,
    overflow: 'hidden',
    textAlign: 'center',
    textAlignVertical: 'center',
    color: '#13b4a7',
    borderWidth: 3,
    borderColor: '#13b4a7',
    fontSize: 22,
    fontWeight: '900',
  },
  reportText: {
    flex: 1,
    color: '#303534',
    fontSize: 19,
    lineHeight: 28,
    fontWeight: '700',
  },
  reportDivider: {
    height: 1,
    marginBottom: 18,
    borderStyle: 'dashed',
    borderWidth: 1,
    borderColor: '#e0e0e0',
  },
  reportHeart: {
    position: 'absolute',
    left: 40,
    bottom: 28,
    color: '#f0c884',
    fontSize: 52,
  },
  reportAnimal: {
    position: 'absolute',
    right: -8,
    bottom: -8,
  },
  roomTop: {
    height: 70,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
  },
  backCircle: {
    width: 48,
    height: 48,
    borderRadius: 999,
    overflow: 'hidden',
    textAlign: 'center',
    textAlignVertical: 'center',
    color: '#ffffff',
    backgroundColor: 'rgba(255,255,255,0.22)',
    fontSize: 36,
  },
  roomTimer: {
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 999,
    overflow: 'hidden',
    color: '#2fb9a9',
    backgroundColor: '#ffffff',
    fontSize: 18,
    fontWeight: '900',
  },
  soundCircle: {
    width: 48,
    height: 48,
    borderRadius: 999,
    overflow: 'hidden',
    textAlign: 'center',
    textAlignVertical: 'center',
    color: '#2fb9a9',
    backgroundColor: '#ffffff',
    fontWeight: '900',
  },
  roomScene: {
    minHeight: 420,
    alignItems: 'center',
    justifyContent: 'center',
    paddingBottom: 18,
  },
  roomWindowShape: {
    position: 'absolute',
    right: 22,
    top: 20,
    width: 118,
    height: 164,
    borderRadius: 28,
    borderWidth: 5,
    borderColor: 'rgba(255,255,255,0.28)',
    backgroundColor: 'rgba(115,155,190,0.28)',
  },
  roomStumpOne: {
    position: 'absolute',
    left: 28,
    bottom: 78,
    width: 74,
    height: 40,
    borderRadius: 999,
    backgroundColor: '#9d7a5d',
  },
  roomStumpTwo: {
    position: 'absolute',
    left: 80,
    bottom: 62,
    width: 62,
    height: 34,
    borderRadius: 999,
    backgroundColor: '#8d6d55',
  },
  roomBucket: {
    position: 'absolute',
    right: 34,
    bottom: 100,
    alignItems: 'center',
    gap: 4,
  },
  roomBucketText: {
    color: '#ffffff',
    fontSize: 12,
    fontWeight: '900',
  },
  snowText: {
    position: 'absolute',
    color: 'rgba(255,255,255,0.86)',
    fontSize: 18,
  },
  snowOne: {
    left: 56,
    top: 86,
  },
  snowTwo: {
    right: 70,
    top: 116,
  },
  snowThree: {
    left: 150,
    top: 54,
  },
  roomWindow: {
    position: 'absolute',
    left: 26,
    top: 18,
    color: 'rgba(255,255,255,0.72)',
    fontWeight: '900',
  },
  roomName: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: '900',
    marginTop: 10,
  },
  voiceBars: {
    color: '#ffffff',
    fontSize: 34,
    fontWeight: '900',
    marginTop: 70,
  },
  aiHint: {
    color: 'rgba(255,255,255,0.72)',
    marginTop: 30,
  },
  roomMessageStack: {
    position: 'absolute',
    left: 14,
    right: 14,
    bottom: 172,
    gap: 8,
    zIndex: 4,
  },
  roomBubble: {
    maxWidth: '88%',
    paddingHorizontal: 12,
    paddingVertical: 9,
    borderRadius: 16,
    backgroundColor: 'rgba(255,255,255,0.86)',
  },
  roomBubbleUser: {
    alignSelf: 'flex-end',
    backgroundColor: 'rgba(218,247,241,0.94)',
  },
  roomBubbleCompanion: {
    alignSelf: 'flex-start',
  },
  roomBubbleAuthor: {
    color: '#2fb9a9',
    fontSize: 11,
    fontWeight: '900',
    marginBottom: 3,
  },
  roomBubbleText: {
    color: '#314a42',
    lineHeight: 19,
  },
  roomInputDock: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    padding: 12,
    backgroundColor: '#ffffff',
  },
  roomPlus: {
    width: 42,
    height: 42,
    borderRadius: 999,
    overflow: 'hidden',
    textAlign: 'center',
    textAlignVertical: 'center',
    color: '#263d36',
    backgroundColor: '#f4f8f6',
    fontSize: 24,
    fontWeight: '900',
  },
  roomInput: {
    flex: 1,
    height: 42,
    paddingHorizontal: 12,
    borderRadius: 14,
    color: '#263d36',
    backgroundColor: '#f4f8f6',
  },
  roomSend: {
    height: 42,
    paddingHorizontal: 16,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#bfeee8',
  },
  roomSendText: {
    color: '#ffffff',
    fontWeight: '900',
  },
  noteComposer: {
    padding: 16,
    borderRadius: 28,
    marginBottom: 16,
    backgroundColor: '#ffffff',
  },
  moodRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginVertical: 12,
  },
  moodChip: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: '#eef5f2',
  },
  moodChipActive: {
    backgroundColor: '#d9f6f0',
  },
  moodText: {
    color: '#6c7e78',
    fontWeight: '800',
  },
  moodTextActive: {
    color: '#28a993',
  },
  noteInput: {
    minHeight: 96,
    padding: 12,
    borderRadius: 18,
    color: '#263d36',
    backgroundColor: '#f4f8f6',
    textAlignVertical: 'top',
    marginBottom: 12,
  },
  actionRow: {
    flexDirection: 'row',
    gap: 10,
  },
  primaryButton: {
    flex: 1,
    minHeight: 50,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#28b6a9',
  },
  primaryButtonText: {
    color: '#ffffff',
    fontWeight: '900',
  },
  ghostButton: {
    flex: 1,
    minHeight: 50,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#eef5f2',
  },
  ghostButtonText: {
    color: '#4c6860',
    fontWeight: '900',
  },
  memoryNote: {
    padding: 16,
    marginBottom: 10,
    borderRadius: 22,
    backgroundColor: 'rgba(255,255,255,0.13)',
  },
  memoryMeta: {
    color: '#a9d6cf',
    fontWeight: '900',
    marginBottom: 8,
  },
  memoryText: {
    color: '#ffffff',
    lineHeight: 22,
  },
  tabBar: {
    position: 'absolute',
    left: 16,
    right: 16,
    bottom: 12,
    height: 72,
    paddingTop: 8,
    paddingBottom: 10,
    borderTopWidth: 0,
    borderRadius: 30,
    backgroundColor: '#ffffff',
    shadowColor: '#2f574f',
    shadowOpacity: 0.18,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 8 },
    elevation: 9,
  },
  tabLabel: {
    fontSize: 12,
    fontWeight: '900',
    marginBottom: 4,
  },
  tabIcon: {
    fontSize: 18,
    fontWeight: '900',
  },
});
