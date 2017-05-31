package com.esrisy.LuceneDemo;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class OtherDemoCode {

	public void demo1() throws Exception {  
        StringReader reader = new StringReader("新华社首尔11月8日电(记者王家辉)深陷“亲信干政”风波的韩国总统朴槿惠8日表示，愿意任命国会推荐的人选担任国务总理。同日，韩国检方证实“亲信干政”事件核心人物崔顺实确曾审阅政府机密文件，并加紧展开有关她涉腐问题的调查。</br>　　据韩联社报道，朴槿惠8日前往国会，与议长丁世均进行了约13分钟的会晤。朴槿惠表示，作为总统，最大的职责就是要使国政正常运行，如果国会推荐经朝野协商后的人选，她将任命其为国务总理并实质性总管内阁。此前，朴槿惠提名卢武铉总统时期幕僚金秉准为新总理人选，但遭到在野党方面的强烈反对。韩国媒体认为，朴槿惠8日的表态意味着她撤回了对金秉准的提名。</br>　　对此，执政党新国家党表示欢迎，敦促朝野就此进行协商；而在野党则要求朴槿惠“完全退居二线”。</br>　　韩联社报道说，司法部门8日表示，检方特别检察组对崔顺实使用过的平板电脑进行分析后，确认其中200多份文档极大部分是政府正式公开前的机密文件，其中包括朴槿惠演讲稿、与朝鲜非公开接触的资料、国务会议资料等。</br>　　检方还从青瓦台前附属秘书郑虎成的手机中发现崔顺实要求其转达政府具体文件的相关录音，郑虎成在接受调查时表示，转达文件是经由朴槿惠指示。目前，检方正以郑虎成为对象，就崔顺实干政程度进行集中调查。</br>　　特别检察组8日说，将于19日左右就崔顺实干政问题提起公诉。检方还表示，目前正在就直接调查朴槿惠的方案进行讨论，最早将于下周敲定是否调查、调查方式和时间等。朴槿惠此前表示，愿就“亲信干政”事件接受检方调查，并与朝野加强沟通。</br>　　此外，检方8日对位于首尔瑞草区的三星电子总部外联室、三星外联社长兼韩国马术协会会长朴商镇的办公室等地进行了长达11个小时的突击搜查，并没收了相关电脑硬盘以及各类资料等。同时，朴商镇私宅、韩国马术协会、韩国马事协会等其他9处地点也遭到突击搜查。</br>　　三星涉嫌在2015年以咨询费名义向崔顺实在德国的公司提供280万欧元(约合人民币2097万元)资金，检方认为三星存在贿赂总统亲信以牟利的嫌疑。</br>　　8日，韩国民众抗议持续。全国144个舞蹈团体、岭南大学100名教授、忠清北道远东大学学生会、京畿中央地方律师协会等团体当天通过发表时局宣言、举行签名活动等形式，抗议朴槿惠“亲信干政”事件，要求其下台。</br>");  
        IKSegmenter ik = new IKSegmenter(reader, true);// 当为true时，分词器进行最大词长切分
        Lexeme lexeme = null;
        try {
            while ((lexeme = ik.next()) != null)
                System.out.println(lexeme.getLexemeText());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();  
        }
    }
    
    public void demo2() throws IOException {
    	System.out.println( "Hello World!" );
        IKAnalyzer analyzer = new IKAnalyzer(true);
        System.out.println("当前使用的分词器：" + analyzer.getClass().getSimpleName());
        TokenStream tokenStream = analyzer.tokenStream("content", "王五好吃懒做，溜须拍马，跟着李四，也过着小康的日子 诛仙 林光远"); 
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();// 必须先调用reset方法  
        while (tokenStream.incrementToken()) {
            System.out.println(charTermAttribute.toString());
        }
        tokenStream.close();
    }
}
