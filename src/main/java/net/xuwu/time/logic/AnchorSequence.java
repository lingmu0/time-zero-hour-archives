package net.xuwu.time.logic;

import java.util.Arrays;
import java.util.Random;

/** Server-owned secret: only the accepted prefix is rendered. */
public final class AnchorSequence {
    private int[] order = {0,1,2,3};
    private int progress;
    public void randomize(long seed) {
        order=new int[]{0,1,2,3};progress=0;var random=new Random(seed);
        for(int i=3;i>0;i--){int j=random.nextInt(i+1),v=order[i];order[i]=order[j];order[j]=v;}
    }
    public boolean press(int index) {
        if(index<0||index>=4||progress==4)return false;
        if(index!=order[progress]){progress=0;return false;}
        progress++;return true;
    }
    public int progress(){return progress;}
    public int mask(){int mask=0;for(int i=0;i<progress;i++)mask|=1<<order[i];return mask;}
    public int[] order(){return order.clone();}
    public boolean restore(int[] saved,int progress){
        if(saved.length!=4||!Arrays.equals(Arrays.stream(saved).sorted().toArray(),new int[]{0,1,2,3}))return false;
        order=saved.clone();this.progress=Math.max(0,Math.min(4,progress));return true;
    }
}
